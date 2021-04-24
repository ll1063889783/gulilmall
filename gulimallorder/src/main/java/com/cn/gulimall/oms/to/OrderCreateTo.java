package com.cn.gulimall.oms.to;

import com.cn.gulimall.oms.entity.OrderEntity;
import com.cn.gulimall.oms.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;//订单计算的应付价格

    private BigDecimal fare;//运费


}
