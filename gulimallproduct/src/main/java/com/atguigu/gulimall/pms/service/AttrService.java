package com.atguigu.gulimall.pms.service;

import com.atguigu.gulimall.pms.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.AttrEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:25
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    public void saveAttr(AttrVo attr) throws InvocationTargetException, IllegalAccessException;

    PageUtils queryBasePage(Map<String, Object> params, Long catelogId);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

