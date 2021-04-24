package com.atguigu.gulimall.ums.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value="gulimall-member")
public interface OrderFeignService {

    /**
     * 查询当前登录用户的所有订单
     * @param params
     * @return
     */
    @RequestMapping("/listWithItem")
    //@RequiresPermissions("oms:order:list")
    public R listWithItem(@RequestBody Map<String, Object> params);
}
