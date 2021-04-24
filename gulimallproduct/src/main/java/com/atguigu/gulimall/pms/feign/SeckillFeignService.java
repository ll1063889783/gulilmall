package com.atguigu.gulimall.pms.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.pms.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用远程接口失败之后的回调类
 */
@FeignClient(value="gulimall-seckill",fallback=SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {
    /**
     * 根据skuId查询商品的秒杀信息（调用秒杀服务）
     * @param skuId
     * @return
     */
    @GetMapping(value="/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable(value="skuId") Long skuId);
}
