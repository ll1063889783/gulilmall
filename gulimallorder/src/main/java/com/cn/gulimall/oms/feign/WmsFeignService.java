package com.cn.gulimall.oms.feign;

import com.atguigu.common.utils.R;
import com.cn.gulimall.oms.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value="gulimall-ware")
public interface WmsFeignService {

    @PostMapping(value="/wms/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
