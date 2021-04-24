package com.atguigu.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.SpuImagesEntity;

import java.util.Map;

/**
 * spuͼƬ
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:25
 */
public interface SpuImagesService extends IService<SpuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

