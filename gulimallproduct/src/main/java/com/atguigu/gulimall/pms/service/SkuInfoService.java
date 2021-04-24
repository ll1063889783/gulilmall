package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.pms.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:25
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuInfoEntity getBySkuId(Long skuId);

    SkuItemVo item(Long skuId);
}

