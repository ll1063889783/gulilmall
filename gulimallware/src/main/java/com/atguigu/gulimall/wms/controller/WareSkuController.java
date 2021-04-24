package com.atguigu.gulimall.wms.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.wms.exception.NoStockException;
import com.atguigu.gulimall.wms.vo.SkuHasStockVo;
import com.atguigu.gulimall.wms.vo.WareSkuLockVo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.wms.entity.WareSkuEntity;
import com.atguigu.gulimall.wms.service.WareSkuService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:57:11
 */
@RestController
@RequestMapping("wms/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //查询sku是否有库存
    @PostMapping(value="/hasStock")
    public R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> skuIds){
            List<SkuHasStockVo> vos = wareSkuService.getSkusHasStock(skuIds);
            R<List<SkuHasStockVo>> ok = R.ok();
            ok.setData(vos);
            return ok;
    }

    @PostMapping(value="/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo) throws InvocationTargetException, IllegalAccessException {
        try{
            Boolean stock = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch(NoStockException e){
            return R.error();
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("wms:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("wms:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("wms:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("wms:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("wms:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
