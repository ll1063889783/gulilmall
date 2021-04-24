package com.atguigu.gulimall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:24
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

