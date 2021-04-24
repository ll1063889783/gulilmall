package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constants.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.*;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService{

    //调用es客户端
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;
    //去es进行检索
    @Override
    public SearchResult search(SearchParamVo searchParamVo) {
        //1，动态构建出查询需要的dsl语句
        SearchResult result = null;
        //准备检索请求
        SearchRequest searchRequest = bulidSearchRequest(searchParamVo);

        try {
            //执行es检索返回检索结果
            SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //分析响应数据封装成我们需要的格式
            result = bulidSearchRequest(searchResponse,searchParamVo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 构建结果数据
     * @param searchResponse
     * @return
     */
    private SearchResult bulidSearchRequest(SearchResponse searchResponse,SearchParamVo searchParamVo) {
        SearchResult result = new SearchResult();
        //返回的所有查询到的商品
        SearchHits hits = searchResponse.getHits();

        List<SkuEsModel> esModels = new ArrayList<SkuEsModel>();
        SearchHit[] searchHits = hits.getHits();
        if(searchHits!=null && searchHits.length!=0){
            for (SearchHit searchHit : searchHits) {
                String sourceAsString = searchHit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(!StringUtils.isEmpty(searchParamVo.getKeyword())){
                    //获取高亮信息
                    HighlightField skuTitle = searchHit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProductList(esModels);
        List<AttrVo> attrVos = new ArrayList<>();

        //当前所有商品涉及到的所有属性信息
        ParsedNested attr_agg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            AttrVo attrVo = new AttrVo();
            //1,得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //2，得到属性的名字
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //3,得到属性的所有值
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attr_value_agg.getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            //前端用来判断哪些属性用来检索过
            result.getAttrIds().add(attrId);
            attrVos.add(attrVo);
        }
        result.setAttrVos(attrVos);
        //当前所有商品涉及到的所有品牌信息
        List<BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            BrandVo brandVo = new BrandVo();
            //得到品牌的id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //得到品牌的名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //得到品牌的图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImage = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImage);
            brandVos.add(brandVo);
        }
        result.setBrandVos(brandVos);
        //当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        List<CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            CatalogVo catalogVo = new CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogVos(catalogVos);
        //页码
        result.setPageNum(searchParamVo.getPageNum());
        //分页信息-总记录数
        long total = hits.getTotalHits();
        result.setTotal(total);
        //分页信息-总页码
        int totalPages = (total%EsConstant.PRODUCT_PAGESIZE == 0)?(int)(total/EsConstant.PRODUCT_PAGESIZE) : (int)(total/EsConstant.PRODUCT_PAGESIZE+1);
        result.setTotalPages(totalPages);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i=1;i<=totalPages;i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        if(searchParamVo.getAttrs()!=null && searchParamVo.getAttrs().size()>0){
            //构建面包屑导航功能
            List<NavVo> navVoList = searchParamVo.getAttrs().stream().map(attr -> {
                //分析每一个attrs传过来的查询参数 attrs=2_5寸:6寸
                String[] s = attr.split("_");
                NavVo navVo = new NavVo();
                navVo.setNavValue(s[1]);
                R info = productFeignService.info(Long.parseLong(s[0]));
                //远程调用成功
                if(info.getCode() == 0){
                    AttrResponseVo attr1 = (AttrResponseVo) info.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attr1.getAttrName());

                } else {
                    navVo.setNavName(s[0]);
                }
                //request.getQueryString()获取的是编码后的地址，url中包含中文和特殊字符、空格(%20)将被转义
                //取消了面包屑之后，我们要跳转到那个地方，将请求地址的url里面的当前置空
                //拿到所有的查询条件，去掉当前
                String replace = replaceGetString(searchParamVo, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVoList);
        }
        //品牌，分类
        if(searchParamVo.getBrandId()!=null && searchParamVo.getBrandId().size()>0){
            List<NavVo> navs = result.getNavs();
            NavVo navVo = new NavVo();
            navVo.setNavName("品牌");
            R info = productFeignService.info(searchParamVo.getBrandId());
            if (info.getCode() == 0){
                List<BrandVo> brand = (List<BrandVo>)info.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                String replace = "";
                StringBuffer buffer = new StringBuffer();
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()+";");
                    replace = replaceGetString(searchParamVo,brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavName(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
            }
        }
        return result;
    }

    private String replaceGetString(SearchParamVo searchParamVo, String attr,String key) {
        String encode = null;
        try {
            //解决url编码问题，让其编码之后才能识别中文
            encode = URLEncoder.encode(attr, "UTF-8");
            encode = encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return searchParamVo.get_queryString().replace("&"+key+"=" + encode, "");
    }

    /***
     * 准备检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
     * @return
     */
    private SearchRequest bulidSearchRequest(SearchParamVo searchParamVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构建boolQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1,must模糊匹配,前台搜索条件
        if(!StringUtils.isEmpty(searchParamVo.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",searchParamVo.getKeyword()));
        }
        //3级分类不为空 按照3级分类查询
        if(!StringUtils.isEmpty(searchParamVo.getCatalog3Id())){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",searchParamVo.getCatalog3Id()));
        }
        //根据品牌列表id查询
        if(searchParamVo.getBrandId()!=null && searchParamVo.getBrandId().size()>0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",searchParamVo.getBrandId()));
        }
        //根据库存是否有进行查询
        if(searchParamVo.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",searchParamVo.getHasStock() == 1));
        }
        //根据价格区间检索 1_500,_500,500_
        if(!StringUtils.isEmpty(searchParamVo.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = searchParamVo.getSkuPrice().split("_");
            if(s.length==2){
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if(s.length==1){
                if(searchParamVo.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if(searchParamVo.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        //按照所有指定的属性进行查询
        if(searchParamVo.getAttrs()!=null && searchParamVo.getAttrs().size()>0){
            //attrs=1_5寸：8寸&attrs=1_16G:8G
            for (String attr : searchParamVo.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] ss = attr.split("_");
                String attrId = ss[0];//检索的属性id
                //这个属性的检索用的值
                String[] attrValues = ss[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs",nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }
        //把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);
        if(!StringUtils.isEmpty(searchParamVo.getSort())){
            String sort = searchParamVo.getSort();
            String[] split = sort.split("_");
            SortOrder order = split[1].equalsIgnoreCase("asc")? SortOrder.ASC: SortOrder.DESC;
            sourceBuilder.sort(split[0],order);
        }
        //分页
        sourceBuilder.from((searchParamVo.getPageNum()-1)*EsConstant.PRODUCT_PAGESIZE.intValue());
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE.intValue());

        //高亮显示
        if(!StringUtils.isEmpty(searchParamVo.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        /**
         * 聚合分析
         */
        //品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        //Todo 聚合brand
        sourceBuilder.aggregation(brand_agg);
        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //分类聚合的子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        //Todo 聚合catalog
        sourceBuilder.aggregation(catalog_agg);
        //属性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attr_id对应的所有可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        //Todo 聚合attr
        sourceBuilder.aggregation(attr_agg);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
