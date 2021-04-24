package com.atguigu.gulimall.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * spu基本分组展示属性vo
 */
@Data
public class SpuItemAttrGroupVo {
    //分组的名字
    private String groupName;
    //分组下面的多对属性的属性值
    private List<SpuBaseAttrVo> attrs;
}
