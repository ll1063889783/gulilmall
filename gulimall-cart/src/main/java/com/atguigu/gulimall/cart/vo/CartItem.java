package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物项
 */
//@Data
public class CartItem {

    //商品的skuId
    private Long skuId;
    //商品是否选中
    private Boolean check = true;
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

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 计算当前购物项的总价
     * @return
     */
    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = this.price.multiply(new BigDecimal(count+""));
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
