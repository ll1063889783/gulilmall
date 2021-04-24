package com.atguigu.gulimall.ums.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/info/{id}")
    //@RequiresPermissions("sms:coupon:info")
    public R info(@PathVariable("id") Long id);
}
