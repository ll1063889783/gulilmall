package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void searchLoads() throws IOException {

        //1,创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //2,指定索引
        searchRequest.indices("bank");
        //指定dsl，索引条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构造索引条件

        searchSourceBuilder.query(QueryBuilders.matchQuery("address","sss"));
        //按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        //计算平均薪资
        AvgAggregationBuilder avgAgg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(avgAgg);
        searchRequest.source(searchSourceBuilder);

        //执行检索
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hitsHit : hitsHits) {
            String string = hitsHit.getSourceAsString();
            Map map = JSON.parseObject(string, Map.class);
        }

        //分析结果
        Aggregations aggregations = searchResponse.getAggregations();
        /*for (Aggregation aggregation : aggregations.asList()) {

        }*/
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();

        }
    }

    /**
     * 存储到es
     */
    @Test
    public void contextLoads() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        user.setUserName("liliang");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);//要保存的内容
        //执行操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //提取有用的响应数据

        //indexRequest.source("userName","zhangsan","age",18);
    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }
}
