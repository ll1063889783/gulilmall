package com.cn.gulimall.oms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {
    //收货地址的id
    private Long addrId;
    //支付方式
    private Integer payType;
    //无需提交需要购买的商品，去购物车再获取一遍
    //优惠、发票
    //防重令牌
    private String orderToken;
    private BigDecimal payPrice;//应付价格 验价

    private String note;//订单的备注
    //用户相关信息，直接去session取出登录的用户


}
