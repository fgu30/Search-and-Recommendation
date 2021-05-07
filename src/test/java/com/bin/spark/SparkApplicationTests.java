package com.bin.spark;

import com.alibaba.fastjson.JSON;
import com.bin.spark.model.ShopModel;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SparkApplication.class)
class SparkApplicationTests {

    @Qualifier("highLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {

    }

    /**
     * 创建索引
     * 必须对应版本，spring-boot-2.2.6.RELEASE 支持的版本是 es-6.8.7
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("index");
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request,     RequestOptions.DEFAULT);
        System.out.println("createIndex: " + JSON.toJSONString(createIndexResponse));
    }


    /**
     * 判断索引是否存在
     * @return
     * @throws IOException
     */
    @Test
    public boolean existsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest();
        request.indices("index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("existsIndex: " + exists);
        return exists;
    }

    /**
     * 删除索引
     * @return
     * @throws IOException
     */
    @Test
    public void delIndex() throws IOException {
        boolean exists = existsIndex();
        if(!exists) {
            //不存在就结束
            return ;
        }
        DeleteIndexRequest request = new DeleteIndexRequest("index");
        request.timeout(TimeValue.timeValueMinutes(2));
        request.timeout("2m");
        restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }
//    ----------------------------建议不要在java代码中对索引进行操作------------------------------------------

    /**
     * 新增文档 restHighLevelClient 新增修改都是用index方法
     */
    @Test
    public void addDoc() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException {
        ShopModel shopModel = new ShopModel();
        shopModel.setId(501);
        shopModel.setAddress("江西南昌");
        shopModel.setCategoryId(1);
        shopModel.setName("南昌瓦罐汤");
        shopModel.setPricePerMan(20);
        shopModel.setTags("经典 实惠");

        Map<String ,Object> map = BeanUtils.describe(shopModel);
        IndexRequest indexRequest = new IndexRequest("shop");
        indexRequest.id("501");
        indexRequest.source(map);
        indexRequest.type("_doc");
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 更新文档
     */
    @Test
    public void updDoc() throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShopModel shopModel = new ShopModel();
        shopModel.setId(501);
        shopModel.setAddress("江西南昌");
        shopModel.setCategoryId(1);
        shopModel.setName("南昌瓦罐汤");
        shopModel.setPricePerMan(20);
        shopModel.setTags("经典 实惠个屁");

        Map<String ,Object> map = BeanUtils.describe(shopModel);
        IndexRequest indexRequest = new IndexRequest("shop");
        indexRequest.id("501");
        indexRequest.source(map);
        indexRequest.type("_doc");
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 删除文档
     */
    @Test
    public void delDoc() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("shop");
        deleteRequest.id("501");
        deleteRequest.type("_doc");
        restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
    }

//    -------------------------------搜索-分割线----------------------------------------

    @Test
    public void searchDoc() throws IOException {
        //elasticsearch 返回的门店数据
        List<ShopModel> shopModels =new ArrayList<>();
        //构建高亮体
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        //高亮字段
        highlightBuilder.field("address").field("name");

        //l.利用searchRequest 查询
        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //名称查询
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","酒店"));
        //人均消费排序
        searchSourceBuilder.sort("price_per_man", SortOrder.ASC);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        //高亮
        searchSourceBuilder.highlighter(highlightBuilder);

        Script script = new Script("distance");
        searchSourceBuilder.scriptField("distance", script,true);

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse=restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits  = searchResponse.getHits().getHits();
        for(SearchHit hit:hits){
            System.out.println(hit.getSourceAsMap().toString());
        }

    }
}
