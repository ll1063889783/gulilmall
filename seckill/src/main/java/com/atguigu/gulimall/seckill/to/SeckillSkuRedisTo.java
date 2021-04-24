package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * redis里面存储的秒杀商品的信息
 */
@Data
public class SeckillSkuRedisTo {

    private Long promotionId;
    /**
     *
     */
    private Long promotionSessionId;
    /**
     *
     */
    private Long skuId;
    /**
     *
     */
    private BigDecimal seckillPrice;
    /**
     *
     */
    private BigDecimal seckillCount;
    /**
     * ÿ
     */
    private BigDecimal seckillLimit;
    /**
     *
     */
    private Integer seckillSort;
    //sku的详细信息
    private SkuInfoVo skuInfoVo;
    //当前商品的开始时间
    private Long startTime;
    //秒杀的结束时间
    private Long endTime;

    //商品秒杀的随机码
    private String randomCode;

}
