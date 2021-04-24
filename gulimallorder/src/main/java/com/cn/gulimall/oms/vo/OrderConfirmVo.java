package com.cn.gulimall.oms.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


/**
 * 订单确认页需要用的数据
 */
public class OrderConfirmVo {
    @Getter @Setter
    //收货地址 ums_member_receive_address表
    private List<MemberAddressVo> memberAddressVoList;

    @Getter @Setter
    //所有选中的购物项
    private List<OrderItemVo> items;

    //发票记录
    @Getter @Setter
    //优惠券信息记录
    private Integer integration;

    private BigDecimal total;//订单总额
    @Getter @Setter
    private String orderToken;

    public BigDecimal getTotal() {

        BigDecimal total = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items) {
                BigDecimal decimal = item.getPrice().multiply(new BigDecimal(item.getCount()));
                total = total.add(decimal);
            }
        }
        return total;
    }

    private BigDecimal payPrice;//应付总额

    public BigDecimal getPayPrice() {
        BigDecimal payPrice = new BigDecimal("0");
        if(items!=null){
            for (OrderItemVo item : items) {
                BigDecimal decimal = item.getPrice().multiply(new BigDecimal(item.getCount()));
                payPrice = payPrice.add(decimal);
            }
        }
        return payPrice;
    }

    private Integer count;//商品的总数量

    public Integer getCount(){
        Integer i = 0;
        if(items!=null){
            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }
}
