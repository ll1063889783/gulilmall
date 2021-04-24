package com.atguigu.gulimall.pms.vo;

import lombok.Data;

/**
 * 属性值对应的skuId
 */
@Data
public class AttrValueWithSkuIdVo {
    private String attrValue;//商品的属性值
    //（例如商品属性白色在哪些skuId里面有，动态切换属性组合）
    private String skuIds;//商品的属性值属于哪些skuId集合
}
