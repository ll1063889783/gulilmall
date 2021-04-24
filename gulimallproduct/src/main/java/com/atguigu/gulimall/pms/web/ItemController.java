package com.atguigu.gulimall.pms.web;

import com.atguigu.gulimall.pms.service.SkuInfoService;
import com.atguigu.gulimall.pms.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;
    //展示当前sku的详情
    @GetMapping(value="/{skuId}.html")
    public String skuItem(@PathVariable(value="skuId")Long skuId, Model model){
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";
    }
}
