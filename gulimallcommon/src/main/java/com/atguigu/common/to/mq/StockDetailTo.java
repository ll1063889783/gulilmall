package com.atguigu.common.to.mq;

import lombok.Data;

/**
 * 库存工作单详情To
 */
@Data
public class StockDetailTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     *
     */
    private Long wareId;
    /**
     *
     */
    private Integer stock;
    /**
     * sku_name
     */
    private String skuName;
    /**
     *
     */
    private Integer stockLocked;

    private Integer skuNum;
}
