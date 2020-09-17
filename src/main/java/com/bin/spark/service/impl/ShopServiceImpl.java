package com.bin.spark.service.impl;

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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public Map<String, Object> searchEs(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderBy, Integer categoryId, String tags) throws IOException {
        Map<String, Object> result = new HashMap<>(1);

        SearchRequest searchRequest = new SearchRequest("shop");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name",keyword));
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(searchSourceBuilder);

        List<Integer> shopIdList = new ArrayList<>();
        SearchResponse searchResponse=restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits  = searchResponse.getHits().getHits();
        for(SearchHit hit:hits){
            shopIdList.add(new Integer(hit.getSourceAsMap().get("id").toString()));
        }
        List<ShopModel>  shopModels = shopIdList.stream().map(this::get).collect(Collectors.toList());

        result.put("shop",shopModels);
        return result;
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
    }
}
