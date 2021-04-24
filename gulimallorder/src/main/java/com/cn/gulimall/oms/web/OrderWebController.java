package com.cn.gulimall.oms.web;

import com.cn.gulimall.oms.service.OrderService;
import com.cn.gulimall.oms.vo.OrderConfirmVo;
import com.cn.gulimall.oms.vo.OrderSubmitVo;
import com.cn.gulimall.oms.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;
    /**
     * 从购物车列表点击去结算按钮跳转至订单结算页
     * 用户的状态必须是登录状态，不然直接跳到登录页
     * @return
     */
    @GetMapping(value="/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData",orderConfirmVo);
        return "orderConfirm";
    }

    /**
     * 订单详情页点击去结算的下单功能
     * @param vo
     * @return
     */
    @PostMapping(value = "/sumbitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes){
        //创建订单,验令牌，验价格，锁库存。。。。
        //下单成功则跳转下单支付页
        //下单失败则回到订单页面重新下单
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
        if(responseVo.getCode() == 0){
            //下单成功来到支付页面
            model.addAttribute("orderResp",responseVo);
            return "pay";
        } else {
            String msg="下单失败：";
            switch(responseVo.getCode()){
                case 1: msg+="订单信息过期，请刷新再次提交";
                break;
                case 2: msg+="订单商品价格发生变化，请确认后再次提交";
                break;
                case 3: msg+="库存锁定失败，商品库存不足";
                break;
            }
            //模拟session的作用域
            attributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}

