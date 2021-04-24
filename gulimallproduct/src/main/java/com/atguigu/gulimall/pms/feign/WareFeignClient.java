package com.atguigu.gulimall.pms.feign;

import com.atguigu.common.to.SkuHasStockVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignClient {

    @PostMapping(value="/wms/waresku/hasStock")
    public R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> skuIds);
}
