package com.atguigu.gulimall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * 商品销售属性vo
 */
@Data
public class ItemSaleAttrsVo {

    private Long attrId;

    private String attrName;
    //原来使用属性的集合用，分隔（无法动态选择销售属性版本）修改为包含的list集合
    private List<AttrValueWithSkuIdVo> attrValues;

}
