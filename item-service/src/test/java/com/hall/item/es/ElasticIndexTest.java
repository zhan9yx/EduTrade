package com.hall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.ItemApplication;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;

// 要以java11运行
@ContextConfiguration(classes = ItemApplication.class)
@SpringBootTest(properties = "spring.profiles.active=local") // 激活配置操作数据库
public class ElasticIndexTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    @Test
    void testConnection(){
        System.out.println("client: " + client);
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

    @Test
    void testAddDocument() throws IOException {
        // 获取Item
        Item item = itemService.getById(317578L);
        // 转为ItemDoc
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String doc = JSONUtil.toJsonStr(itemDoc);
        // 准备request
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 请求体
        request.source(doc, XContentType.JSON);
        // 发送
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocument() throws IOException {
        // 请求
        GetRequest request = new GetRequest("items", "317578");
        // 发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String res = response.getSourceAsString();
        // 解析响应结果
        ItemDoc itemDoc = JSONUtil.toBean(res, ItemDoc.class);
        System.out.println("itemDoc = " + itemDoc);
    }

    @Test
    void testDeleteDocument() throws IOException {
        // 请求
        DeleteRequest request = new DeleteRequest("items", "317578");
        // 发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDocument() throws IOException {
        // 请求
        UpdateRequest request = new UpdateRequest("items", "317578");
        // 发送请求
        request.doc("price", 29900);
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulk() throws IOException {
        int pageNo = 1, pageSize = 500;

        while (true) {
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));

            List<Item> items = page.getRecords();

            if (items == null || items.isEmpty()) {
                return;
            }

            BulkRequest request = new BulkRequest();

            for (Item item : items) {
                request.add(new IndexRequest("items").id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)), XContentType.JSON));
            }

            client.bulk(request, RequestOptions.DEFAULT);
            pageNo++;
        }
    }
}
