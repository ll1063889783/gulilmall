package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer{

    @Autowired
    private CartInterceptor cartInterceptor;
    /**
     * 配置购物车拦截器信息，匹配所有路径
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor).addPathPatterns("/**");
    }
}
