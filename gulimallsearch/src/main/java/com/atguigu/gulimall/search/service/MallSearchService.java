package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;

public interface MallSearchService {
    /**
     *
     * @param searchParamVo 检索的所有参数
     * @return 返回检索的结果
     */
    SearchResult search(SearchParamVo searchParamVo);
}
