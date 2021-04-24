package com.cn.gulimall.oms.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean(value = "requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //老请求
                HttpServletRequest request = requestAttributes.getRequest();
                if(request!=null){
                    //同步请求头数据，Cookie
                    String cookie = request.getHeader("Cookie");
                    //给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie",cookie);
                }
            }
        };
    }
}
