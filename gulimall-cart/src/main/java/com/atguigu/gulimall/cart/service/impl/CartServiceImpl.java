package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    private static final String CART_PREFIX = "gulimall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String res = (String) cartOps.get(skuId.toString());

        //购物车中无此商品
        if(StringUtils.isEmpty(res)){
            CartItem cartItem = new CartItem();
            //添加新商品到购物车
            CompletableFuture<Void> skuInfo1 = CompletableFuture.runAsync(() -> {
                //1，远程查询当前要添加的商品信息
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = (SkuInfoVo) info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfo.getPrice());
            }, executor);

            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                //远程查询sku的组合信息
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, executor);
            //等待两个线程全部执行完
            CompletableFuture.allOf(skuInfo1, getSkuSaleAttrValues).get();
            String cartStr = JSON.toJSONString(cartItem);
            //把当前购物项加入到redis中
            cartOps.put(skuId.toString(), cartStr);
            return cartItem;
        } else {
            //购物车中有此商品，修改数量即可,重新修改redis
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            cartOps.put(skuId.toString(),JSON.toJSONString(cartItem));
            return cartItem;
        }

    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(res, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId()!= null){
            //1、用户已经登录了
            String cartKey = CART_PREFIX + userInfoTo.getUserId();

            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //如果临时购物车的数据还没有进行合并
            //获取临时购物车数据
            List<CartItem> tempCartItem = getCartItems(tempCartKey);
            //临时购物车有数据，需要合并
            if(tempCartItem!=null && tempCartItem.size()>0){
                for (CartItem cartItem : tempCartItem) {
                    //把临时购物车数据添加至在线购物车中
                    addToCart(cartItem.getSkuId(),cartItem.getCount());
                }
                //清除临时购物车的数据
                clearCart(tempCartKey);
            }
            //3，获取登录后的购物车的数据[包含合并过来的临时购物车和登录后的购物车的数据]
            List<CartItem> cartItemList = getCartItems(cartKey);
            cart.setItems(cartItemList);
        } else {
            //2,用户没有登录
            String cartKey = CART_PREFIX+userInfoTo.getUserKey();
            //获取临时购物车
            List<CartItem> tempCartItems = getCartItems(cartKey);
            cart.setItems(tempCartItems);
        }
        return cart;
    }

    /**
     * 获取临时用户购物车
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        //获取所有的值集合
        List<Object> values = hashOps.values();
        List<CartItem> cartItems = new ArrayList<CartItem>();
        if(values!=null && values.size()>0){
            cartItems = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
        }
        return cartItems;
    }
    @Override
    public void clearCart(String cartKey){
        this.redisTemplate.delete(cartKey);
    }

    @Override
    public void checkCartItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked==1?true:false);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void changeCartItemNum(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),s);
    }

    @Override
    public void deleteCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.getUserId() == null){
            return null;
        } else {
            String cartKey = CART_PREFIX+userInfoTo.getUserId();
            //获取redis里面的购物车数据（购物车中商品价格可能会有变动）
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter((item) -> {
                return item.getCheck()== true;
            }).map((items)->{
                R r = productFeignService.getSkuPrice(items.getSkuId());
                //更新为最新价格，远程查询商品服务
                return items;
            }).collect(Collectors.toList());
            return collect;
        }

    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        //用户登录了
        if (userInfoTo.getUserId() != null) {
            //redis中的key为前缀＋userId;
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //临时购物车的redis中的key为前缀＋user-key的值
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //加入购物车的时候判断redis中是否有该购物项，如果有就更新购物项商品数量，
        //如果没有该购物项则给redis里面新增该购物项。
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        return hashOperations;
    }
}
