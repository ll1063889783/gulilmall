package com.atguigu.gulimall.wms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    //商品的skuId
    private Long skuId;
    //商品标题
    private String title;
    //商品图片
    private String image;
    //商品的sku属性集合
    private List<String> skuAttr;
    //商品的单价
    private BigDecimal price;
    //商品的数量
    private Integer count;
    //商品的总价，前端页面可以根据商品数量的增减计算商品总价
    private BigDecimal totalPrice;
}
