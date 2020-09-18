package com.bin.spark.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bin.spark.common.BaseException;
import com.bin.spark.common.BusinessException;
import com.bin.spark.common.EmBusinessError;
import com.bin.spark.mapper.ShopModelMapper;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.model.SellerModel;
import com.bin.spark.model.ShopModel;
import com.bin.spark.service.CategoryService;
import com.bin.spark.service.SellerService;
import com.bin.spark.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by 斌~
 * 2020/9/14 16:18
 * @author mac
 */
@Service
@Slf4j
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopModelMapper shopModelMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /**
     * 创建门店
     *
     * @param shopModel
     * @return
     */
    @Override
    @Transactional
    public ShopModel create(ShopModel shopModel) throws BaseException{
        //校验商家
        SellerModel sellerModel = sellerService.get(shopModel.getSellerId());
        if(sellerModel == null){
            throw new BaseException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户不存在");
        }

        if(sellerModel.getDisabledFlag() == 1){
            throw new BaseException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商户已禁用");
        }
        //校验类目
        CategoryModel categoryModel = categoryService.get(shopModel.getCategoryId());
        if(categoryModel == null){
            throw new BaseException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"类目不存在");
        }
        //校验名称
        Example example = new Example(ShopModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", shopModel.getName());
        List<ShopModel> shopModels = shopModelMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(shopModels)){
            throw new RuntimeException("门店信息已经存在！");
        }
        //创建门店
        shopModel.setCreatedAt(new Date());
        shopModel.setUpdatedAt(new Date());
        shopModelMapper.insertSelective(shopModel);
        return shopModel;
    }

    /**
     * 查询门店
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public ShopModel get(Integer id) {
        ShopModel shopModel = shopModelMapper.selectByPrimaryKey(id);
        if(shopModel == null){
            return null;
        }
        shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
        shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        return shopModel;
    }

    /**
     * 门店列表
     *
     * @return
     */
    @Override
    @Transactional
    public List<ShopModel> selectAll() {
        List<ShopModel> shopModelList = shopModelMapper.selectAll();
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    /**
     * 根据经纬度查询
     *
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    @Transactional(rollbackFor =Exception.class)
    public List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitude) {
        List<ShopModel> shopModelList = shopModelMapper.recommend(longitude, latitude);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    /**
     * 根据经纬度以及地理位置查询
     *
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderby
     * @param categoryId
     * @param tags
     * @return
     */
    @Override
    @Transactional(rollbackFor =Exception.class)
    public List<ShopModel> search(BigDecimal longitude, BigDecimal latitude, String keyword,
                                  Integer orderby, Integer categoryId, String tags) {
        List<ShopModel> shopModelList = shopModelMapper.search(longitude,latitude,keyword,orderby,categoryId,tags);
        shopModelList.forEach(shopModel -> {
            shopModel.setSellerModel(sellerService.get(shopModel.getSellerId()));
            shopModel.setCategoryModel(categoryService.get(shopModel.getCategoryId()));
        });
        return shopModelList;
    }

    /**
     * 结果标签查询
     *
     * @param keyword
     * @param categoryId
     * @param tags
     * @return
     */
    @Override
    public List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags) {

        return shopModelMapper.searchGroupByTags(keyword,categoryId,tags);
    }

    @Override
    public Integer countAllShop() {
        ShopModel shopModel  = new ShopModel();
        return shopModelMapper.selectCount(shopModel);
    } /**
     * 根据经纬度以及地理位置查询(elasticSearch)
     *
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderBy
     * @param categoryId
     * @param tags
     * @return
     */
    @Override
    @Transactional
    public Map<String, Object> searchEs(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderBy, Integer categoryId, String tags) throws IOException {
        Map<String, Object> result = new HashMap<>(1);
        List<ShopModel>  shopModels =new ArrayList<>();
        //l.利用searchRequest 查询
//        SearchRequest searchRequest = new SearchRequest("shop");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query(QueryBuilders.matchQuery("name",keyword));
//        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        searchRequest.source(searchSourceBuilder);
//        List<Integer> shopIdList = new ArrayList<>();
//        SearchResponse searchResponse=restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        SearchHit[] hits  = searchResponse.getHits().getHits();
//        for(SearchHit hit:hits){
//            shopIdList.add(new Integer(hit.getSourceAsMap().get("id").toString()));
//        }
//       shopModels = shopIdList.stream().map(this::get).collect(Collectors.toList());

        //2.直接用elasticsearch语句  http请求,但是如果碰到可传可不传的参数，拼写语句及其不方便。
//        Request request = new Request("GET","/shop/_search");
//        String reqJson = "{\n" +
//                "  \"_source\": \"*\",\n" +
//                "  \"script_fields\": {\n" +
//                "    \"distance\": {\n" +
//                "      \"script\": {\n" +
//                "        \"source\": \"haversin(lat,lon,doc['location'].lat,doc['location'].lon)\",\n" +
//                "        \"lang\": \"expression\"\n" +
//                "        , \"params\": {\"lat\":"+latitude.toString()+",\"lon\":"+longitude.toString()+"}\n" +
//                "      }\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"query\":{\n" +
//                "    \"function_score\": {\n" +
//                "      \"query\": {\n" +
//                "        \"bool\": {\n" +
//                "          \"must\": [\n" +
//                "            {\"match\": {\"name\":{\"query\":\""+keyword+"\"}}},\n" +
//                "            {\"term\": {\"seller_disabled_flag\":0}}\n" +
//                "          ]\n" +
//                "        }\n" +
//                "      },\n" +
//                "      \"functions\": [\n" +
//                "        {\n" +
//                "          \"gauss\": {\n" +
//                "            \"location\": {\n" +
//                "              \"origin\": \""+latitude.toString()+","+longitude.toString()+"\",\n" +
//                "              \"scale\": \"100km\",\n" +
//                "              \"offset\": \"0km\",\n" +
//                "              \"decay\": 0.5\n" +
//                "            }\n" +
//                "          },\n" +
//                "          \"weight\": 9\n" +
//                "        },\n" +
//                "        {\n" +
//                "          \"field_value_factor\": {\n" +
//                "            \"field\": \"remark_score\"\n" +
//                "          },\n" +
//                "          \"weight\": 0.2\n" +
//                "        },\n" +
//                "        {\"field_value_factor\": {\n" +
//                "          \"field\": \"seller_remark_score\"\n" +
//                "        },\n" +
//                "        \"weight\": 0.2\n" +
//                "        }\n" +
//                "      ],\n" +
//                "      \"boost_mode\": \"sum\",\n" +
//                "      \"score_mode\": \"sum\"\n" +
//                "    }\n" +
//                "  },\n" +
//                "  \"sort\": [\n" +
//                "    {\n" +
//                "      \"_score\": {\n" +
//                "        \"order\": \"desc\"\n" +
//                "      }\n" +
//                "    }\n" +
//                "  ]\n" +
//                "}";
//        log.info(reqJson);
//        request.setJsonEntity(reqJson);
//        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
//        String responseStr = EntityUtils.toString(response.getEntity());
//        log.info(responseStr);
//        JSONObject jsonObject = JSONObject.parseObject(responseStr);
//        JSONArray jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
//        for (int i = 0; i < jsonArray.size(); i++) {
//            JSONObject jsonObj = jsonArray.getJSONObject(i);
//            Integer id = Integer.parseInt(jsonObj.get("_id").toString());
//            BigDecimal distance  = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
//            ShopModel shopModel = get(id);
//            shopModel.setId(id);
//            shopModel.setDistance(distance.multiply(new BigDecimal("1000").setScale(0,BigDecimal.ROUND_UP)).intValue());
//            shopModels.add(shopModel);
//        }

        Request request = new Request("GET","/shop/_search");
        //1.构建请求
        JSONObject jsonRequestObject = new JSONObject();
        //2.构建source
        jsonRequestObject.put("_source","*");
        //3.构建自定义请求 script_fields
        jsonRequestObject.put("script_fields",new JSONObject());
        jsonRequestObject.getJSONObject("script_fields").put("distance",new JSONObject());
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").put("script",new JSONObject());
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("source","haversin(lat,lon,doc['location'].lat,doc['location'].lon)");
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("lang", "expression");
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .put("params",new JSONObject());
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lat",latitude);
        jsonRequestObject.getJSONObject("script_fields").getJSONObject("distance").getJSONObject("script")
                .getJSONObject("params").put("lon",longitude);

        //4.构建query  start!=========================================================================================
        jsonRequestObject.put("query",new JSONObject());
        jsonRequestObject.getJSONObject("query").put("function_score",new JSONObject());
        jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("query",new JSONObject());
        jsonRequestObject.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").put("bool",new JSONObject());
        JSONArray mustArray = new JSONArray();
        if(categoryId !=null){
            JSONObject categoryObj = new JSONObject();
            categoryObj.put("term",new JSONObject());
            categoryObj.getJSONObject("term").put("category_id",categoryId);
            mustArray.add(categoryObj);
        }
        JSONObject matchObj = new JSONObject();
        matchObj.put("match",new JSONObject());
        matchObj.getJSONObject("match").put("name",new JSONObject());
        matchObj.getJSONObject("match").getJSONObject("name").put("query",keyword);
        matchObj.getJSONObject("match").getJSONObject("name").put("boost",0.1);
        mustArray.add(matchObj);
        JSONObject termObj = new JSONObject();
        termObj.put("term",new JSONObject());
        termObj.getJSONObject("term").put("seller_disabled_flag",0);
        mustArray.add(termObj);
        jsonRequestObject.getJSONObject("query").getJSONObject("function_score").getJSONObject("query").getJSONObject("bool").put("must",mustArray);
        //query END ==================================================================================================

        //functions构建
        JSONArray jsonArray = new JSONArray();
        if(orderBy == null){
            jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("boost_mode","sum");
            jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("score_mode","sum");
            //function -> gauss
            JSONObject gauss = new JSONObject();
            gauss.put("gauss",new JSONObject());
            gauss.put("weight",9);
            gauss.getJSONObject("gauss").put("location",new JSONObject());
            gauss.getJSONObject("gauss").getJSONObject("location").put("origin",latitude.toString()+","+longitude.toString());
            gauss.getJSONObject("gauss").getJSONObject("location").put("scale","100km");
            gauss.getJSONObject("gauss").getJSONObject("location").put("offset","0km");
            gauss.getJSONObject("gauss").getJSONObject("location").put("decay",0.5);
            jsonArray.add(gauss);
            //function _field_value_factor -> remark_score
            JSONObject remark_score = new JSONObject();
            remark_score.put("weight",0.2);
            remark_score.put("field_value_factor",new JSONObject());
            remark_score.getJSONObject("field_value_factor").put("field","remark_score");
            jsonArray.add(remark_score);
            //function _field_value_factor -> seller_remark_score
            JSONObject seller_remark_score = new JSONObject();
            seller_remark_score.put("weight",0.2);
            seller_remark_score.put("field_value_factor",new JSONObject());
            seller_remark_score.getJSONObject("field_value_factor").put("field","seller_remark_score");
            jsonArray.add(seller_remark_score);
        }else{
            //order by 低价排序
            //boost_mode functions 的排序得分替换query的排序得分
            jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("boost_mode","replace");
            jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("score_mode","sum");

            JSONObject seller_remark_score = new JSONObject();
            seller_remark_score.put("weight",1);
            seller_remark_score.put("field_value_factor",new JSONObject());
            seller_remark_score.getJSONObject("field_value_factor").put("field","price_per_man");
            jsonArray.add(seller_remark_score);
        }
        jsonRequestObject.getJSONObject("query").getJSONObject("function_score").put("functions",jsonArray);
        //5.构建排序sort
        JSONArray sortArray =  new JSONArray();
        JSONObject sortJsonObject = new JSONObject();
        sortJsonObject.put("_score",new JSONObject());
        if(orderBy == null ){
            sortJsonObject.getJSONObject("_score").put("order","desc");
        }else{
            //低价排序
            sortJsonObject.getJSONObject("_score").put("order","asc");
        }

        sortArray.add(sortJsonObject);
        jsonRequestObject.put("sort",sortArray);

        log.info(jsonRequestObject.toJSONString());
        request.setJsonEntity(jsonRequestObject.toJSONString());

        Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
        String responseStr = EntityUtils.toString(response.getEntity());
        log.info(responseStr);
        JSONObject jsonObject = JSONObject.parseObject(responseStr);
        JSONArray jsonArrayHits = jsonObject.getJSONObject("hits").getJSONArray("hits");
        for (int i = 0; i < jsonArrayHits.size(); i++) {
            JSONObject jsonObj = jsonArrayHits.getJSONObject(i);
            Integer id = Integer.parseInt(jsonObj.get("_id").toString());
            BigDecimal distance  = new BigDecimal(jsonObj.getJSONObject("fields").getJSONArray("distance").get(0).toString());
            ShopModel shopModel = get(id);
            shopModel.setId(id);
            shopModel.setDistance(distance.multiply(new BigDecimal("1000").setScale(0,BigDecimal.ROUND_UP)).intValue());
                shopModels.add(shopModel);
        }
        result.put("shop",shopModels);
        return result;
    }
}
