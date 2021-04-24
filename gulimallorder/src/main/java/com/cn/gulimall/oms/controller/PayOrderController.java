package com.cn.gulimall.oms.controller;

import com.alipay.api.AlipayApiException;
import com.cn.gulimall.oms.config.AlipayTemplate;
import com.cn.gulimall.oms.service.OrderService;
import com.cn.gulimall.oms.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayOrderController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 1，将支付页让浏览器展示
     * 2，支付成功以后，我们要跳转到用户的订单列表页
     * @param orderSn
     * @return
     */
    @ResponseBody
    @GetMapping(value="/payOrder",produces = "text/html")
    public String payOrder(@RequestParam(value="orderSn")String orderSn) throws AlipayApiException {
        /*PayVo payVo = new PayVo();
        payVo.setBody();//订单的备注
        payVo.setOut_trade_no();//订单号
        payVo.setSubject();//订单的主题
        payVo.setTotal_amount();//*/
        PayVo payVo = orderService.getOrderPay(orderSn);
        //返回的一个页面，将此页面直接交给浏览器就行。
        String pay = alipayTemplate.pay(payVo);
        //返回的一个form表单和一段script代码（会自动提交form表单），浏览器解析该代码。
        System.out.println(pay);
        return pay;
    }
}
