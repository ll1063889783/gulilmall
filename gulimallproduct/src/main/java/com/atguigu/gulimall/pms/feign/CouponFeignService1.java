package com.atguigu.gulimall.pms.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gulimall-coupon")
public interface CouponFeignService1 {

}
