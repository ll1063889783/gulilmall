package com.atguigu.gulimall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.pms.service.CategoryBrandRelationService;
import com.atguigu.gulimall.pms.vo.Catelog2Vo;
import com.atguigu.gulimall.pms.vo.Catelog3Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.pms.dao.CategoryDao;
import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/***
 * Spring-Cache的不足
 *  1，读模式
 *     缓存穿透：查询一个null数据。解决方案：缓存空数据
 *     #是否缓存空值，防止缓存穿透的问题
 spring.cache.redis.cache-null-values=true
 缓存击穿：大量并发同时查询一个正好过期的数据。解决方案：加锁
 默认是无加锁的，在@Cacheable里面加入sync=true（加锁解决击穿问题）
 缓存雪崩：大量的key同时过期。解决方案： 加随机时间，加上过期时间
 spring.cache.redis.time-to-live=3600000
 2，写模式(缓存与数据库一致)
 1，读写加锁
 2，引入Canal，感知到mysql的更新去更新数据库
 3，读多写多的，直接去数据库查询就行
 总结：常规数据（读多写少，即时性，一致性要求不高的数据）完全可以使用
 Spring-Cache:写模式 只要缓存的数据有过期时间就足够了。
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private RedissonClient redisson;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //找到所有一级分类
        List<CategoryEntity> rootList = categoryEntities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return rootList;
    }
    //触发将数据从缓存删除的操作 ,失效模式(执行方法的时候会删除缓存中指定的key),指定缓存区（category）的名字，指定从缓存区中删除指定的key（查询方法的名字作为key，字符串和常量要加单引号）

    /**
     * 1,同时进行多种缓存操作 @Caching
     * 2,指定删除某个分区下的所有数据,指定allEntries = true
     *
     * @CacheEvict(value={"category"},allEntries = true)
     * 3,存储同一类型的数据，都可以指定为同一分区，分区名默认就是缓存的前缀
     * 就是为缓存区名字::key的名字作为key
     */
    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getLevel1Categorys'"),
            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    /***
     * 每一个需要缓存的数据我们都来指定要放到哪个名字的缓存。【缓存的分区(按照业务类型)】
     * 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     * 如果缓存中没有，会调用方法，最后将方法的结果放入缓存。
     * key默认为category::SimpleKey[](自主生成) 缓存的名字::SimpleKey[]
     * 缓存的value的值为，默认使用java序列化机制，将序列化后的数据存到redis
     * 默认时间为TTL=-1,表示永不过期。
     * 自定义的操作。
     *  1,指定生成的缓存使用的key：key属性指定，接受一个SpEl表达式
     *  #root开头来取值,#root.method.name用当前方法的方法名做为key的名字
     *  2,指定缓存的数据的存活时间,application.properties中修改ttl时间
     *  缓存的存活时间毫秒为单位(1小时)，相当为缓存的过期时间TTL
     spring.cache.redis.time-to-live=3600000
     *  3，将数据保存为json格式
     *   CacheAutoConfiguration 导入
     *   RedisCacheConfiguration 自动配置了
     *   RedisCacheManager 初始化所有的缓存
     *   每个缓存决定使用什么配置
     *   如果redisCacheConfiguration有就用已有的，没有就用默认配置
     *  想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration
     *  就会应用到当前RedisCacheManager管理的所有缓存分区中
     *   */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        System.out.println("查询了数据库。。。。。");
        //1,将数据库里面的多次查询变成一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(Level1 -> Level1.getCatId().toString(), Level1v -> {

            //每一个一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, Level1v.getCatId());
            List<Catelog2Vo> catelog2VoList = null;
            if (categoryEntities != null) {
                //每一个二级分类，查到这个二级分类的三级分类
                catelog2VoList = categoryEntities.stream().map(Level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(Level1v.getCatId().toString(), null, Level2.getCatId().toString(), Level2.getName());

                    List<CategoryEntity> level3CatelogList = getParent_cid(selectList, Level2.getCatId());
                    if (level3CatelogList != null) {
                        List<Catelog3Vo> catelog3VoList = level3CatelogList.stream().map(Level3 -> {
                            Catelog3Vo catelog3Vo = new Catelog3Vo(Level2.getCatId().toString(), Level3.getCatId().toString(), Level3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatelog3List(catelog3VoList);
                    }
                    return catelog2Vo;

                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));
        return map;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        //给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型。
        /**
         * 1,空结果缓存，解决缓存穿透
         * 2，设置过期时间（加随机值），解决缓存雪崩
         * 3，加锁，解决缓存击穿
         */
        //1,加入缓存逻辑，缓存中的数据是json字符串
        //json跨语言，跨平台兼容
        String catalogJson = (String) redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            //2,缓存中没有，查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return stringListMap;
    }

    /**
     * 使用Redisson分布式锁来解决高并发的问题
     * 缓存里面的数据如何和数据库保持一致
     * 1，双写模式（改数据库的同时把缓存也更新）会产生脏数据（高并发下出现数据问题）
     * 给缓存的key加过期时间
     * 2，失效模式（修改数据库的同时把缓存给删了）也会产生脏数据
     * 两者都会实现缓存的最终一致性。     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //1,锁的名字，锁的粒度，越细越好。
        //锁的粒度：具体缓存的是某个数据。11号商品，product-11-lock,product-12-lock

        RLock lock = redisson.getLock("catalogJson-lock");
        //加锁
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            //必须等待下面的逻辑执行完成才解锁。
            lock.unlock();
        }
        return dataFromDb;

    }

    /**
     * 使用redis分布式锁来解决高并发的问题
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        //1,占分布式锁，去redis占坑 setIfAbsent相当于setNx命令操作 setnx命令是如果没有该key则可以执行。
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功。。。。");
            //加锁成功！执行业务
            //设置过期时间
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                //获取值对比＋对比成功删除=原子操作 lua脚本删除
                String script = "if redis.call('get',KEY[1]) == ARGV[1] then return redis.call('del',KEY[1]) else return 0 end";
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
           /* String lockValue = (String) redisTemplate.opsForValue().get("lock");
            if(uuid.equals(lockValue)){
                //删除redis里面的key，其他线程可以再次获取锁。
                redisTemplate.delete("lock");
            }*/
            return dataFromDb;
        } else {
            //加锁失败！要重试 synchronized
            //休眠100ms之后重试
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catalogJson = (String) redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //如果缓存中不为null
            Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return stringListMap;
        }
        System.out.println("查询了数据库。。。。。");
        //1,将数据库里面的多次查询变成一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> map = level1Categorys.stream().collect(Collectors.toMap(Level1 -> Level1.getCatId().toString(), Level1v -> {

            //每一个一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, Level1v.getCatId());
            List<Catelog2Vo> catelog2VoList = null;
            if (categoryEntities != null) {
                //每一个二级分类，查到这个二级分类的三级分类
                catelog2VoList = categoryEntities.stream().map(Level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(Level1v.getCatId().toString(), null, Level2.getCatId().toString(), Level2.getName());

                    List<CategoryEntity> level3CatelogList = getParent_cid(selectList, Level2.getCatId());
                    if (level3CatelogList != null) {
                        List<Catelog3Vo> catelog3VoList = level3CatelogList.stream().map(Level3 -> {
                            Catelog3Vo catelog3Vo = new Catelog3Vo(Level2.getCatId().toString(), Level3.getCatId().toString(), Level3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatelog3List(catelog3VoList);
                    }
                    return catelog2Vo;

                }).collect(Collectors.toList());
            }
            return catelog2VoList;
        }));
        //3,查到的数据再放入缓存，将对象转换成json放在缓存中
        String s = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return map;
    }

    /**
     * 使用本地锁解决高并发访问的问题
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //1,加锁，同步代码块，只要是同一把锁就能锁住这个锁的所有线程
        //synchronized(this),springboot所有的组件在容器中都是单例的。
        //Todo 本地锁，synchronized，juc（Lock）在分布式情况下，想要锁住所有必须要分布式锁。
        synchronized (this) {
            //得到锁之后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", Level1v.getCatId().toString()));
        List<CategoryEntity> collect = selectList.stream().filter((item) -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());
        return collect;
    }
    /*private List<CategoryEntity> getChildren(List<CategoryEntity> rootList,List<CategoryEntity> all){
        List<CategoryEntity> childrenList = null;
        for(CategoryEntity categoryEntity : rootList){
             childrenList = new ArrayList<>();
            for (CategoryEntity categoryEntity1 : all){
                if(categoryEntity.getCatId() == categoryEntity1.getParentCid()){
                     childrenList.add(categoryEntity1);
                     categoryEntity.setChildren(childrenList);
                }
            }
            getChildren(childrenList,all);
        }
        return rootList;
    }*/

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param categoryEntity
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity categoryEntity, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(category -> {
            return category.getParentCid() == categoryEntity.getCatId();
        }).map(category -> {
            //找到子菜单
            category.setChildren(getChildren(category, all));
            return category;
        }).sorted((menu1, menu2) -> {
            //菜单排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}