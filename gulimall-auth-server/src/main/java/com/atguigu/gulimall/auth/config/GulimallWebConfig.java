package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通过映射地址跳转至对应的页面，配置类
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer{
    /**
     * 视图映射
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        //registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("regist");
    }
}
