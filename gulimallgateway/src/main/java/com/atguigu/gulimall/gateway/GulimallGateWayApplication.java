package com.atguigu.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *如何使用nacos配置中心管理配置
 * 1，引入依赖
 * 2，创建一个bootstrap.properties文件配置上
 * spring.application.name=gulimall-coupon
   spring.cloud.nacos.config.server-addr=127.0.0.1:8848
  3，在配置中心中加入一个应用名.properties或者应用名.yml其中应用名为
  spring.application.name的值
  4,动态获取配置
  加入@RefreshScope注解    @Value
 如果配置中心和当前应用的配置文件中都配置了相同的项，优先使用
 配置中心的配置，优先级是bootstrap.properties(yml)>配置中心的配置＞application.properties(yml)
  其中细节
    1，命名空间，配置隔离
    默认public(保留空间)，默认新增的所有配置都在public空间
    可以利用命名空间进行环境隔离
    在bootstrap.properties配置上，需要使用哪个命名空间下
    spring.cloud.nacos.config.namespace=8e6228ca-600c-4979-897d-c8dc3a379f09
   每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
   2，配置集 所有的配置的集合
   3，配置集ID：类型配置文件名
      Data ID : 配置文件名
   4，配置分组
   默认所有的配置集都属于： DEFAULT_GROUP
  1111,1212,618 双十一、双十二、六一八
 每个微服务创建自己的命名空间，使用配置分组区分环境，dev、test、prod
 微服务任何配置信息，任何配置文件都可以放在配置中心
 只需要在bootstrap.properties说明加载配置中心哪些配置文件即可
 配置中心有的优先使用配置中心的。

 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallGateWayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallGateWayApplication.class,args);
    }
}
