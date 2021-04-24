package com.atguigu.gulimall.wms.vo;

import lombok.Data;

@Data
public class LockStockResult {
    private Long skuId;//商品的skuId
    private Integer num;//库存锁定数量
    private Boolean locked;//库存是否锁定
}
