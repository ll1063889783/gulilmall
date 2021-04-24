package com.atguigu.gulimall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * mybatis-plus逻辑删除操作步骤
 * 1，配置全局的逻辑删除规则
 *      mybatis-plus:
            global-config:
                db-config:
                    id-type: auto
                    logic-delete-value: 1
                    logic-not-delete-value: 0
   2，配置逻辑删除的组件bean（mybatis-plus3.0以上版本可以省略）
   3，给实体bean加上逻辑删除注解@TableLogic
 */
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
 *  1,开启缓存功能 @EnableCaching
 *  2,只需要使用注释就能完成缓存操作
 */
@EnableRedisHttpSession//开启SpringSessionRedis
@EnableCaching
@EnableFeignClients(basePackages = "com.atguigu.gulimall.pms.feign")
@EnableDiscoveryClient
@MapperScan("com.atguigu.gulimall.pms.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class,args);
    }
}
