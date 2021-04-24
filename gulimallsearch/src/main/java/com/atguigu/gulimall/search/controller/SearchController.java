package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParamVo;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * @param searchParamVo
     * @param model
     * @return
     */
    @GetMapping(value="/list.html")
    public String listPage(SearchParamVo searchParamVo, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        searchParamVo.set_queryString(queryString);
        //根据传递过来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(searchParamVo);
        model.addAttribute("result",result);
        return "list";
    }
}
