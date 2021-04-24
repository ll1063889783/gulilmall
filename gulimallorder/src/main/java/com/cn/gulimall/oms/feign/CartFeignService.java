package com.cn.gulimall.oms.feign;

import com.cn.gulimall.oms.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value="gulimall-cart")
public interface CartFeignService {

    @GetMapping(value="/currentUserCartItems")
    public List<OrderItemVo> getCurrentUserCartItems();
}
