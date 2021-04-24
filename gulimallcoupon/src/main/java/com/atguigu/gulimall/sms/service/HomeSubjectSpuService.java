package com.atguigu.gulimall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.sms.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * ר
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:16:47
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

