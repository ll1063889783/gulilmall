package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 购物车
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;
    /**
     * 浏览器有一个cookie：user-key:标识用户身份，一个月后过期。
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份：
     * 浏览器以后保存，每次访问都会带上这个cookie
     *
     * 登录了，session有
     * 没登录，按照cookie里面带来的user-key来做。
     * 第一次，如果cookie里面没有临时用户，帮忙创建一个临时用户。
     * @param
     * @return
     */
    //首页点击购物车或者鼠标放在购物车图标上显示所有购物车
    @GetMapping(value = "/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        /*//1，快速得到用户信息,id,user-key
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute == null){
            //没登录获取临时购物车数据
        } else {
            //获取登录了的购物车
        }*/
        //获取当前用户的cookie信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart = cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }

    @GetMapping(value="/currentUserCartItems")
    public List<CartItem>  getCurrentUserCartItems(){
        List<CartItem> cartItems = cartService.getUserCartItems();
        return cartItems;
    }

    /**
     * 在前台页面勾选购物的选中与不选中，更改redis中购物项的选中状态
     * @param skuId 商品的skuId
     * @param checked 商品购物项数据是否选中
     * @return
     */
    @GetMapping(value="/checkItem")
    public String checkItem(@RequestParam(value="skuId") Long skuId,@RequestParam(value="checked")Integer checked){

        cartService.checkCartItem(skuId,checked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 修改购物项的数量
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping(value="/changeCartItemNum")
    public String changeCartItemNum(@RequestParam(value="skuId") Long skuId,@RequestParam(value="num")Integer num){
        cartService.changeCartItemNum(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping(value="/deleteCartItem")
    public String deleteCartItem(@RequestParam(value="skuId") Long skuId){
        cartService.deleteCartItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "num")Integer num,
                            RedirectAttributes redirect) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId,num);
        //redirect.addFlashAttribute("") 模拟session的作用域,将数据放在session里面可以在页面取出，但是只能取一次
        //redirect.addAttribute("skuId",skuId); 将数据放在url后面
        redirect.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 跳转到成功页
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam(value = "skuId") Long skuId,Model model){
        //重定向到成功页面，再次查询购物车数据即可
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item",item);
        return "success";
    }
}
