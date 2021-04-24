package com.cn.gulimall.oms.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.cn.gulimall.oms.config.AlipayTemplate;
import com.cn.gulimall.oms.service.OrderService;
import com.cn.gulimall.oms.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 支付宝给我们的程序发送订单支付成功消息之后的回调地址
 * 告诉支付宝我们收到了消息然后必须返回success字符串
 */
@RestController
public class OrderPayedListener {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    @PostMapping(value="/payed/notify")
    public String handleAlipayed(PayAsyncVo vo,HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        //只要我们收到支付宝给我们的异步的通知，告诉我们订单支付成功。返回
        //success，支付宝就在也不通知了。
        Map<String, String[]> map = request.getParameterMap();
        //验签
        //获取支付宝GET过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名
        //验签成功
        if(signVerified){
            //处理支付宝的支付结果
            String result = orderService.handlePayResult(vo);
            return "success";
        } else {
            //验签失败
            return "error";
        }

    }
}
