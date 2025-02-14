package com.hall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.item.ItemApplication;
import com.hmall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//@ContextConfiguration(classes = ItemApplication.class)
//@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchTest {

    private RestHighLevelClient client;

    @Test
    void testConnection(){
        System.out.println("client: " + client);
    }

    @Test
    void testMatchAll() throws IOException {
        // 请求对象
        SearchRequest request = new SearchRequest("items");
        // 组织DSL参数
        // request.source().query(QueryBuilders.matchAllQuery());
        request.source().query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("name","脱脂牛奶"))
                .filter(QueryBuilders.termQuery("brand.keyword", "德亚"))
                .filter(QueryBuilders.rangeQuery("price").lt(30000)));
        // 发送
        SearchResponse resp = client.search(request, RequestOptions.DEFAULT);
        // 解析
        parseResponse(resp);
    }

    @Test
    void testSortAndPage() throws IOException {
        int pageNo = 1, pageSize = 5;
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery());
        request.source().from((pageNo - 1) * pageSize).size(pageSize);
        request.source().sort("sold", SortOrder.DESC)
                        .sort("price", SortOrder.ASC);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponse(response);
    }

    @Test
    void testHighlight() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"));
        request.source().highlighter(SearchSourceBuilder.highlight().field("name"));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        parseResponse(response);
    }

    @Test
    void testAggr() throws IOException {
        SearchRequest request = new SearchRequest("items");
        // 分页 只返回聚合对象
        request.source().size(0);
        // 聚合
        String aggrBucketName = "brand_aggr";
        request.source().aggregation(
                AggregationBuilders.terms(aggrBucketName).field("brand.keyword").size(10)
        );

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // System.out.println("response = " + response);
        Aggregations aggregations = response.getAggregations();
        // Aggregation是顶级父类接口，aggregation不一定都有分组，所以不会有getBuckets的方法
        // Aggregation aggregation = aggregations.get(aggrBucketName);
        Terms brandTerms = aggregations.get(aggrBucketName);
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("brand = " + keyAsString);
            long docCount = bucket.getDocCount();
            System.out.println("docCount = " + docCount);
        }
    }

    private static void parseResponse(SearchResponse resp) {
        SearchHits searchHits = resp.getHits();
        long records = searchHits.getTotalHits().value;
        System.out.println("records = " + records);
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
            // 高亮判断
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if (hfs != null && !hfs.isEmpty()) {
                // 是高亮请求
                HighlightField hf = hfs.get("name");
                // 取出 highlight name 数组第一个元素（没超过阈值的话取第一个元素即可） 再转为string
                String hfName = hf.getFragments()[0].toString();
                itemDoc.setName(hfName);
            }
            System.out.println("itemDoc = " + itemDoc);
        }
    }


    @BeforeEach
    void setUp(){
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("192.168.88.130:9200")
        ));
    }

    @AfterEach
    void tearDown(){
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
