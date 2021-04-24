package com.atguigu.gulimall.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillInfoVo {
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

    //当前商品的开始时间
    private Long startTime;
    //秒杀的结束时间
    private Long endTime;

    //商品秒杀的随机码
    private String randomCode;
}
