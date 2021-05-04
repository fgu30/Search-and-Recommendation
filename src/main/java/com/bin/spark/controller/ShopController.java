package com.bin.spark.controller;


import com.bin.spark.common.*;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.model.ShopModel;
import com.bin.spark.service.CategoryService;
import com.bin.spark.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端搜索功能
 * @author mac
 */
@Controller("/shop")
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private CategoryService categoryService;

    //推荐服务V1.0
    @RequestMapping("/recommend")
    @ResponseBody
    public ResponseVo<List<ShopModel>> recommend(@RequestParam(name="longitude")BigDecimal longitude,
                                @RequestParam(name="latitude")BigDecimal latitude) throws BaseException {
        if(longitude == null || latitude == null){
            throw new BaseException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        List<ShopModel> shopModelList = shopService.recommend(longitude,latitude);
        return ResponseVo.success(shopModelList);
    }


    //搜索服务V1.0
    @RequestMapping("/search")
    @ResponseBody
    public ResponseVo<Map<String,Object>> search(@RequestParam(name="longitude")BigDecimal longitude,
                            @RequestParam(name="latitude")BigDecimal latitude,
                            @RequestParam(name="keyword")String keyword,
                            @RequestParam(name="orderby",required = false)Integer orderby,
                            @RequestParam(name="categoryId",required = false)Integer categoryId,
                            @RequestParam(name="tags",required = false)String tags) throws BaseException, IOException {
        if(StringUtils.isEmpty(keyword) || longitude == null || latitude == null){
            throw new BaseException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        List<ShopModel> shopModelList = shopService.search(longitude,latitude,keyword,orderby,categoryId,tags);
//        List<Map<String,Object>> tagsAggregation1 = shopService.searchGroupByTags(keyword,categoryId,tags);
        Map<String,Object> map= shopService.searchEs(longitude, latitude, keyword, orderby, categoryId, tags);

        List<ShopModel> shopModelList = (List<ShopModel>) map.get("shops");
        List<Map<String,Object>> tagsAggregation = (List<Map<String, Object>>) map.get("tags");
        List<CategoryModel> categoryModelList = categoryService.selectAll();

        Map<String,Object> resMap = new HashMap<>(3);
        resMap.put("shop",shopModelList);
        resMap.put("category",categoryModelList);
        resMap.put("tags",tagsAggregation);
        return ResponseVo.success(resMap);

    }




}
