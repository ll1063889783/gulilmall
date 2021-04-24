package com.cn.gulimall.oms.config;

import com.cn.gulimall.oms.interceptor.OrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallOrderConfig implements WebMvcConfigurer{

    @Autowired
    private OrderInterceptor orderInterceptor;
    /**
     * 配置购物车拦截器信息，匹配所有路径
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).addPathPatterns("/order/*");
    }
}
