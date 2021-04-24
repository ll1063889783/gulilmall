package com.atguigu.gulimall.wms.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value="gulimall-order")
public interface OrderFeignService {
    /**
     * 获取订单的支付状态
     * @param orderSn
     * @return
     */
    @GetMapping(value="/oms/order/getOrderStatus/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
