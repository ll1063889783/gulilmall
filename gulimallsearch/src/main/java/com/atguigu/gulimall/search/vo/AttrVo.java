package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class AttrVo {
    //属性id
    private Long attrId;
    //属性名
    private String attrName;
    //属性值集合
    private List<String> attrValue;
}
