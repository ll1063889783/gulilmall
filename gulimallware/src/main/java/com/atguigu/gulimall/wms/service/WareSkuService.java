package com.atguigu.gulimall.wms.service;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.wms.vo.OrderVo;
import com.atguigu.gulimall.wms.vo.SkuHasStockVo;
import com.atguigu.gulimall.wms.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.wms.entity.WareSkuEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:57:11
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    /**
     * 订单锁定库存
     * @param vo
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    Boolean orderLockStock(WareSkuLockVo vo) throws InvocationTargetException, IllegalAccessException;

    void unlockStock(StockLockedTo to);

    void unlockStock(OrderTo to);
}

