package com.atguigu.gulimall.seckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 整合Sentinel
 * 1,导入依赖spring-cloud-starter-alibaba-sentinel
 * 2,下载sentinel的控制台
 * 3,配置sentinel控制台地址信息
 * 4,在控制台调整参数，默认所有的流控设置保存在内存中，重启失效。
 * 每一个微服务都导入actuator:并配合management.endpoints.web.exposure.include=*
 * 自定义sentinel流控返回的数据
 *
 * 使用Sentinel来保护feign远程调用，熔断。 *
 * 	1,调用方的熔断保护：feign.sentinel.enabled=true
 * 	2,调用方手动指定远程服务的降级策略。远程服务被降级处理，默认触发我们的熔断回调方法。
 * 	3，超大流量浏览的时候，必须牺牲一些远程服务。在服务的提供方（远程服务）指定降级策略，提供方是在运行，但是不运行自己的业务逻辑，返回的是默认的降级数据。
 * 	自定义受保护的资源
 * 	  1,代码
 *		try(Entry entry = SphU.entry("seckillSkus")){
 *		 //受保护的业务逻辑
 *		}catch(Exception e){
 *
 *		}
 *	2,基于注解
 *  //返回当前时间可以参与的秒杀商品信息
 //注解的方式限流控制（降级处理后调用blockHandler=“降级后的方法xxsdfd”）
 //blockHandler 函数会在原方法被限流/降级/系统保护的时候调用,而fallback函数会针对所有类型的异常。
 //fallback的方法必须写在本类中，fallbackClass的方法可以写在其他类中，但是方法必须是static方法。
 @SentinelResource(value="getCurrentSeckillSkusResource",blockHandler = "blockHandler",fallback = "fallback")
  无论是1,2方式一定要设置被限流以后的默认返回
  url请求限流控制可以设置统一返回WebCallbackManager.setUrlBlockHandler

 */
@EnableRabbit
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class SeckillApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeckillApplication.class, args);
	}

}
