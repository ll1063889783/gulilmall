package com.atguigu.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 商城的session配置解决session共享问题
 * 默认发的令牌。session=sdfsdff.作用域：当前域（解决session共享问题）
   默认使用json的序列化来序列化对象数据到redis中
 */
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {

    /**
     * cookie的序列化器
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        //扩大cookie的作用域明确指定为主域名
        cookieSerializer.setDomainName("gulimall.com");
        //指定cookie的名字
        cookieSerializer.setCookieName("GULISESSION");
        return cookieSerializer;
    }

    /**
     * redis的序列化器
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
