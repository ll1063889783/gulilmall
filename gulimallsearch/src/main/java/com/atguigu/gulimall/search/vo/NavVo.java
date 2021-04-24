package com.atguigu.gulimall.search.vo;

import lombok.Data;

@Data
public class NavVo {
    private String navName;//面包屑名字
    private String navValue;//面包屑值
    private String link;//取消面包屑跳转至的网页链接
}
