package com.atguigu.gulimall.ums.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ums.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping(value="/memberOrder.html")
    public String memberOrderPage(@RequestParam(value="pageNum",defaultValue = "1") Integer pageNum,
                                  Model model, HttpServletRequest request){
        //获取到支付表给我们传来的所有请求数据
        //request验证签名，如果正确可以去修改。
        //查出当前登录的用户的所有订单列表数据
        Map<String,Object> pageParams = new HashMap<>();
        pageParams.put("page",pageNum);
        R r = orderFeignService.listWithItem(pageParams);
        model.addAttribute("orders",r);
        return "orderList";
    }
}
