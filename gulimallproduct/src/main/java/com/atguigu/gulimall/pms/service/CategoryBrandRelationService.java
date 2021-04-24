package com.atguigu.gulimall.pms.service;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.pms.entity.CategoryBrandRelationEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Map;

/**
 * Ʒ
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-08-09 16:01:24
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(@NotNull(message = "修改必须指定品牌id", groups = (UpdateGroup.class)) @Null(message = "新增不能指定id", groups = (AddGroup.class)) Long brandId, @NotBlank(message = "品牌名不能为空", groups = {UpdateGroup.class, AddGroup.class}) String name);

    void updateCategory(Long catId, String name);
}

