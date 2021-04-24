package com.atguigu.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 线程池配置属性类
 */
@Component
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    //核心线程数
    private Integer coreSize;
    //最大线程数
    private Integer maxSize;
    //线程的最大存活时间
    private Integer keepAliveTime;
}
