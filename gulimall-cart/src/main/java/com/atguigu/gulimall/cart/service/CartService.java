package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 购物车功能接口
 */
public interface CartService {

    /**
     * 将商品添加到购物车
     *
     * @param skuId skuId
     * @param num   购物项数量
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    /**
     * 获取购物车中某个购物项
     *
     * @param skuId
     * @return
     */
    CartItem getCartItem(Long skuId);

    /**
     * 获取整个购物车
     *
     * @return
     */
    Cart getCart() throws ExecutionException, InterruptedException;

    /**
     * 清空购物车数据
     */
    void clearCart(String cartKey);

    /**
     * 勾选购物项(选中与不选中)
     *
     * @param skuId
     * @param checked
     */
    void checkCartItem(Long skuId, Integer checked);

    /**
     * 修改购物项的数量
     *
     * @param skuId
     * @param num
     */
    void changeCartItemNum(Long skuId, Integer num);

    /**
     * 删除购物项
     * @param skuId
     */
    void deleteCartItem(Long skuId);

    List<CartItem> getUserCartItems();

}
