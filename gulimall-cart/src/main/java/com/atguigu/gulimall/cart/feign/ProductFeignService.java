package com.atguigu.gulimall.cart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(value = "gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/pms/skuinfo/info/{skuId}")
    //@RequiresPermissions("pms:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId);

    @GetMapping(value = "/pms/skuinfo/stringList/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable(value = "skuId") Long skuId);

    /**
     * 获取商品的sku的价格
     * @param skuId
     * @return
     */
    @GetMapping(value="/pms/skuinfo/{skuId}/getSkuPrice")
    public R getSkuPrice(@PathVariable(value="skuId") Long skuId);
}
