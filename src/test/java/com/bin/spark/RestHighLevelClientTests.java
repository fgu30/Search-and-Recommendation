package com.bin.spark;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bin.spark.model.ShopModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.yarn.webapp.Params;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.spark.sql.catalyst.plans.logical.Project;
import org.elasticsearch.action.admin.cluster.storedscripts.PutStoredScriptRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.ml.PostDataRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.query.ScriptField;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SparkApplication.class)
@Slf4j
public class RestHighLevelClientTests {

    @Qualifier("highLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void contextLoads() {

    }

    /**
     * ????????????
     * ?????????????????????spring-boot-2.2.6.RELEASE ?????????????????? es-6.8.7
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("index");
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request,     RequestOptions.DEFAULT);
        System.out.println("createIndex: " + JSON.toJSONString(createIndexResponse));
    }


    /**
     * ????????????????????????
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
     * ????????????
     * @return
     * @throws IOException
     */
    @Test
    public void delIndex() throws IOException {
        boolean exists = existsIndex();
        if(!exists) {
            //??????????????????
            return ;
        }
        DeleteIndexRequest request = new DeleteIndexRequest("index");
        request.timeout(TimeValue.timeValueMinutes(2));
        request.timeout("2m");
        restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
    }
//    ----------------------------???????????????java??????????????????????????????------------------------------------------

    /**
     * ???????????? restHighLevelClient ?????????????????????index??????
     */
    @Test
    public void addDoc() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException {
        ShopModel shopModel = new ShopModel();
        shopModel.setId(501);
        shopModel.setAddress("????????????");
        shopModel.setCategoryId(1);
        shopModel.setName("???????????????");
        shopModel.setPricePerMan(20);
        shopModel.setTags("?????? ??????");

        Map<String ,Object> map = BeanUtils.describe(shopModel);
        IndexRequest indexRequest = new IndexRequest("shop");
        indexRequest.id("501");
        indexRequest.source(map);
        indexRequest.type("_doc");
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * ????????????
     */
    @Test
    public void updDoc() throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ShopModel shopModel = new ShopModel();
        shopModel.setId(501);
        shopModel.setAddress("????????????");
        shopModel.setCategoryId(1);
        shopModel.setName("???????????????");
        shopModel.setPricePerMan(20);
        shopModel.setTags("?????? ????????????");

        Map<String ,Object> map = BeanUtils.describe(shopModel);
        IndexRequest indexRequest = new IndexRequest("shop");
        indexRequest.id("501");
        indexRequest.source(map);
        indexRequest.type("_doc");
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * ????????????
     */
    @Test
    public void delDoc() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("shop");
        deleteRequest.id("501");
        deleteRequest.type("_doc");
        restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
    }

//    -------------------------------??????-?????????----------------------------------------

    @Test
    public void searchDoc() throws IOException {
        //elasticsearch ?????????????????????
        List<ShopModel> shopModels =new ArrayList<>();
        //???????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        //????????????
        highlightBuilder.field("address").field("name");

        //l.??????searchRequest ??????
        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //????????????
        searchSourceBuilder.query(QueryBuilders.matchQuery("name","??????"));
        //??????????????????
        searchSourceBuilder.sort("_score", SortOrder.DESC);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //??????
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(5);
        //??????
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.scriptField("distance", new Script("distance"),true);




        searchRequest.source(searchSourceBuilder);
        log.info(searchRequest.source().toString());

        SearchResponse searchResponse=restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        log.info(searchResponse.toString());
        SearchHit[] hits  = searchResponse.getHits().getHits();
        for(SearchHit hit:hits){
            System.out.println(hit.getSourceAsMap().toString());
        }

    }


    @Test
    public void searchTemplate() throws IOException {
//        Request request = new Request("GET","/shop/_search");
        SearchTemplateRequest request = new SearchTemplateRequest();
        request.setRequest(new SearchRequest("shop"));

        request.setScriptType(ScriptType.INLINE);
        request.setScript(
                "{" +
                        "  \"query\": { \"match\" : { \"{{field}}\" : \"{{value}}\" } }," +
                        "  \"size\" : \"{{size}}\"" +
                        "}");
        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("field", "name");
        scriptParams.put("value", "??????");
        scriptParams.put("size", 5);
        request.setScriptParams(scriptParams);
        SearchTemplateResponse response = restHighLevelClient.searchTemplate(request, RequestOptions.DEFAULT);
        SearchHit[] hits  = response.getResponse().getHits().getHits();
        for(SearchHit hit:hits){
            System.out.println(hit.getSourceAsMap().toString());
        }
    }

    @Test
    public void searchRequest() throws IOException {
        //???????????????
        JSONObject jsonRequestObject = new JSONObject();
        //1.json?????????
        jsonRequestObject.put("_source","*");
        jsonRequestObject.put("query",new JSONObject());
        jsonRequestObject.put("script_fields",new JSONObject());
        jsonRequestObject.put("aggs",new JSONObject());
        jsonRequestObject.put("sort",new JSONArray());

        JSONObject query = jsonRequestObject.getJSONObject("query");
        JSONObject script_fields = jsonRequestObject.getJSONObject("script_fields");
        JSONObject aggs = jsonRequestObject.getJSONObject("aggs");
        JSONArray sort = jsonRequestObject.getJSONArray("sort");

        //1.??????sort
        sort.add(new JSONArray().add(new JSONObject().put("_score",new JSONObject().put("order","desc"))));
        //2.??????aggs
        aggs.put("group_by_tags",new JSONObject().put("terms",new JSONObject().put("field","tags")));
        //3.??????script_fields
        script_fields.put("distance",new JSONObject().put("script",new JSONObject()
                .put("source","haversin(lat,lon,doc['location'].lat,doc['location'].lon)")
        ));


        //2.??????query

        log.info(jsonRequestObject.toJSONString());
        //????????????
        Request request = new Request("GET","/shop/_search");
        request.setJsonEntity(jsonRequestObject.toJSONString());
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        
        log.info(EntityUtils.toString(response.getEntity()));

        JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
        JSONArray jsonArrayHits = jsonObject.getJSONObject("hits").getJSONArray("hits");



    }


    @Test
    public void searchXContentBuilder () throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        //?????????????????????
        builder.startObject();
//        {
//            builder.startObject("properties");
//            {
//                builder.startObject("message");
//                {
//                    builder.field("type", "text");
//                }
//                builder.endObject();
//            }
//            builder.endObject();
//        }
        builder.field("_source","*");
        {
            builder.startObject("query");
            {
                builder.startObject("function_score");
                {
                    builder.field("score_mode","sum");
                    builder.field("boost_mode","sum");
                    {
                        builder.startObject("query");
                        {
                            builder.startObject("bool");
                            {
                                builder.startArray("must");
                                {
                                    builder.startObject();
                                    {
                                        builder.startObject("match");
                                        {
                                            builder.startObject("name");
                                            {
                                                builder.field("query","??????");
                                                builder.field("boost",0.1);
                                            }
                                            builder.endObject();
                                        }
                                        builder.endObject();
                                    }
                                    builder.endObject();
                                }
                                {
                                    builder.startObject();
                                    {
                                        builder.startObject("term");
                                        {
                                            builder.field("seller_disabled_flag",0);
                                        }
                                        builder.endObject();
                                    }
                                    builder.endObject();
                                }
                                builder.endArray();
                            }
                            builder.endObject();
                        }
                        builder.endObject();
                    }
                    {
                        builder.startArray("functions");
                        {
                            builder.startObject();
                            builder.field("weight",0.2);
                            {
                                builder.startObject("gauss");
                                {
                                    builder.startObject("location");
                                    {
                                        builder.field("offset","0km");
                                        builder.field("origin","22.54605355,114.02597366");
                                        builder.field("scale","100km");
                                        builder.field("decay",0.5);
                                    }
                                    builder.endObject();
                                }
                                builder.endObject();
                            }
                            builder.endObject();
                        }
                        {
                            builder.startObject();
                            builder.field("weight",0.2);
                            {
                                builder.startObject("field_value_factor");
                                {
                                    //????????????
                                    builder.field("field","seller_remark_score");
                                }
                                builder.endObject();
                            }
                            builder.endObject();
                        }
                        {
                            builder.startObject();
                            builder.field("weight",0.2);
                            {
                                builder.startObject("field_value_factor");
                                {
                                    //????????????
                                    builder.field("field","remark_score");
                                }
                                builder.endObject();
                            }
                            builder.endObject();
                        }
                        builder.endArray();
                    }
                }
                builder.endObject();
            }
            builder.endObject();
        }
        //??????script_fields
        {
            builder.startObject("script_fields");
            {
                builder.startObject("distance");
                {
                    builder.startObject("script");
                    {
                        builder.field("source", "haversin(lat,lon,doc['location'].lat,doc['location'].lon)");
                        builder.field("lang", "expression");
                        builder.startObject("params");
                        {
                            builder.field("lon", 114.02597366);
                            builder.field("lat", 22.54605355);
                        }
                        builder.endObject();
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
        }
        //??????sort
        {
            builder.startArray ("sort");
                builder.startObject();
                {
                    builder.startObject("_score");
                    {
                        builder.field("order", "desc");
                    }
                    builder.endObject();
                }
                builder.endObject();
            builder.endArray();
        }
        //??????aggs
        {
            builder.startObject("aggs");
            {
                builder.startObject("group_by_tags");
                {
                    builder.startObject("terms");
                    {
                        builder.field("field", "tags");
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();


        //????????????
        Request request = new Request("GET","/shop/_search");

//        IndexRequest indexRequest = new IndexRequest();
//        indexRequest.source(builder);
//        NStringEntity nStringEntity = new NStringEntity(indexRequest.source().utf8ToString(), ContentType.APPLICATION_JSON);
//        log.info(EntityUtils.toString(nStringEntity));
//        request.setEntity(nStringEntity);

        String json = Strings.toString(builder);
        log.info(json);
        request.setJsonEntity(json);
        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
//
        log.info(EntityUtils.toString(response.getEntity()));

    }
}
