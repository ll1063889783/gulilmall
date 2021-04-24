package com.atguigu.gulimall.pms.web;

import com.atguigu.gulimall.pms.entity.CategoryEntity;
import com.atguigu.gulimall.pms.service.CategoryService;
import com.atguigu.gulimall.pms.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 整合springCache简化缓存开发
 * 1，引入依赖
 * spring-boot-starter-cache,spring-boot-starter-data-redis
 * 2,写配置
 * 自动配置了哪些
 *  1,CacheAutoConfiguration会导入RedisCacheConfiguration
 * 自动配置好了RedisCacheManager
 *  2,配置使用redis作为缓存
 *  3，测试使用缓存
 *  @Cacheable: 触发将数据保存到缓存的操作
 *  @CacheEvict: 触发将数据从缓存删除的操作
 *  @CachePut: 不影响方法执行更新缓存
 *  @Caching: 组合以上多个操作
 *  @CacheConfig: 在类级别共享缓存的相同配置
 */
@RestController
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        //查出所有的1级分类
        List<CategoryEntity> categoryEntities =  categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }

    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }
    @GetMapping(value="/hello")
    public String hello() throws Exception{
        //1,获取一把锁，只要锁的名字，就是同一把锁。不用担心业务时间过长，锁自动过期被删除。
        //加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，默认也会在30s内自动删除
        RLock myLock = redisson.getLock("myLock");
        try{
            //2，加锁
            myLock.lock();//阻塞式等待，默认加的锁都是30秒
            //锁的自动续期，如果业务超长，运行期间自动给锁续上新的30秒。
            System.out.println("加锁成功，执行业务...."+Thread.currentThread().getId());
            Thread.sleep(3000);
        }catch(Exception e){

        }finally{
            //3,解锁-并将解锁代码没有运行，redisson会不会出现死锁
            System.out.println("解锁成功"+Thread.currentThread().getId());
            //解锁功能
            myLock.unlock();
        }
        return "hello";
    }
    //保证一定能督导最新数据，修改期间，写锁是一个排他锁（互斥锁），读锁是一个共享锁。
    //写锁没有释放读就必须等待
    //写+读：等待写锁释放
    //写+写 相当于阻塞
    //读+读： 相当于无锁，并发读，只会在redis里记录好，所有当前的读锁，他们都会同事加锁成功。
    //读+写：有读锁，写也要等待
    //只要有写的存在，都必须等待。
    @GetMapping("/write")
    public String writeValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rwLock");
        String s = "";
        //加写锁
        RLock rLock = lock.writeLock();
        try{
            //写数据加写锁，读数据加读锁。
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(3000);
            redisTemplate.opsForValue().set("writeValue",s);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/read")
    public String readValue(){
        RReadWriteLock lock = redisson.getReadWriteLock("rwLock");
        String s = "";
        //加读锁
        RLock rLock = lock.readLock();
        try{
            //写数据加写锁，读数据加读锁。
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().get("writeValue");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            rLock.unlock();
        }
        return s;
    }
}
