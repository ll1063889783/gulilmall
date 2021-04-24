package com.cn.gulimall.oms;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1，引用amqp场景：RabbitAutoConfiguration就会自动生效
 * 给容器中加入RabbitTemplate、AmqpAdmin,CachingConnectionFactory、RabbitMessagingTemplate
 * @EnableRabbit
 * @ConfigurationProperties(prefix="spring.rabbitmq")
 @EnableRedisHttpSession
 * 配置文件RabbitProperties
 * 给配置文件中配置 spring.rabbitmq信息
 * @EnableRabbit 开启功能
 * 监听消息：使用@RabbitListener：标注在类或者方法上（监听那些队列即可）
 * @RabbitHandler：标注在方法上(重载区分不同的消息)
 * 本地事务失效问题
 * 同一个对象内事务方法之间互调默认失效，原因是绕过了代理对象，事务使用代理对象来控制的。
 * 解决：使用代理对象来调用事务方法
 * 1， 引入aop-starter;spring-boot-starter-aop 引入了aspectj
 * ,2，@EnableAspectJAutoProxy: 开启aspectj动态代理功能。
 *  以后所有的动态代理都是由aspectj创建的。（即使没有接口也能使用动态代理）
 *  @EnableAspectJAutoProxy(exposeProxy = true)对外暴露代理对象
 * 3，本类互调用对象
 * AopContext.currentProxy()来调用本类方法
 * */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients(value="com.cn.gulimall.oms.feign")
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class,args);
    }
}
