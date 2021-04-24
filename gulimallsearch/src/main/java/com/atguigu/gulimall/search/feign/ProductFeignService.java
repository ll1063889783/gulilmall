package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/pms/attr/info/{attrId}")
    //@RequiresPermissions("pms:attr:info")
    public R info(@PathVariable("attrId") Long attrId);

    @GetMapping("/pms/brand/infos")
    //@RequiresPermissions("pms:brand:info")
    public R info(@RequestParam("brandIds") List<Long> brandId);
}
