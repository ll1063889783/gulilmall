package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 整合redis作为session存储的原理
 * 1，@EnableRedisHttpSession导入了RedisHttpSessionConfiguration.class配置
 * 2，给容器中添加了一个组件
 * 		SessionRepository-->RedisOperationsSessionRepository-->redis操作session
 * 	    session的增删改查的封装类
 * 3，SessionRepositoryFilter--->Filter：session存储的过滤器:每一个请求过来都必须经过过滤器
 *    创建的时候，就自动从容器中获取到了SessionRepository
 *    原始的request，response都被包装SessionRepositoryRequestWrapper,SessionRepositoryResponseWrapper
 *    以后获取session。request.getSession()使用的是SessionRepositoryRequestWrapper的getSession();
 *    wrappedRequest.getSession()--->SessionRepository中获到的。
 *
 *    装饰者模式 只要浏览器不关，session会自动延期，redis中的ttl会自动续期。
 */
//整合redis作为session存储
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GulimallAuthServerApplication.class, args);
	}

}
