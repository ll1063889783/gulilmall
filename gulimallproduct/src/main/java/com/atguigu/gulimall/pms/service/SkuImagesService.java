package com.atguigu.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * skuͼƬ
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:24
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

