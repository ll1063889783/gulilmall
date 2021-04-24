package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping(value="/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData1(vos);
    }

    /**
     * 根据skuId查询商品的秒杀信息（在商品服务里面调用）
     * @param skuId
     * @return
     */
    @GetMapping(value="/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable(value="skuId") Long skuId){
        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData1(to);
    }
    /**
     * 真正的秒杀功能
     * @param killId 秒杀id=商品的promotionId_skuId
     * @param randomCode 商品秒杀的随机码
     * @param num 商品秒杀的数量
     * @return
     */
    @GetMapping(value="/kill")
    public R secKill(@RequestParam("killId") String killId,
                     @RequestParam("randomCode")String randomCode,
                     @RequestParam("num")Integer num){

        String orderSn = seckillService.kill(killId,randomCode,num);
        //判断是否登录
        return R.ok().setData1(orderSn);
    }
}
