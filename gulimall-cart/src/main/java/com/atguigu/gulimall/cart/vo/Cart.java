package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写get方法，保证每次获取属性都会进行计算。
 */
public class Cart {
    //购物项列表
    private List<CartItem> items;

    private Integer countNum;//商品总数量（总件数）

    private Integer countType;//商品总类型数

    private BigDecimal totalAmount;//商品总价

    private BigDecimal reduce = new BigDecimal("0.00");//减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if(items!=null && items.size()>0){
            for (CartItem item : items) {
                count+=1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        //计算购物项总价
        BigDecimal decimal = new BigDecimal("0");
        if(items!=null && items.size()>0){
            for (CartItem item : items) {
                if(item.getCheck()){
                    decimal=decimal.add(item.getTotalPrice());
                }
            }
        }
        //减去优惠总价
        BigDecimal subtract = decimal.subtract(getReduce());
        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
