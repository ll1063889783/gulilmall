package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 */
@Data
public class SearchParamVo {
    private String keyword;//页面传递过来的全文检索条件
    private Long catalog3Id;//三级分类

    private String sort;//排序条件

    private Integer hasStock;//是否只显示有货

    private String skuPrice;//价格区间

    private List<Long> brandId;//按照品牌id进行查询，可以多选

    private List<String> attrs;//按照属性进行筛选

    private Integer pageNum=1;//页码

    private String _queryString;//原生的所有查询条件
}
