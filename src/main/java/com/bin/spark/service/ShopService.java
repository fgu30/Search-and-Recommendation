package com.bin.spark.service;

import com.bin.spark.common.BaseException;
import com.bin.spark.common.BusinessException;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.model.ShopModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by 斌~
 * 2020/9/14 11:00
 */
public interface ShopService {

    /**
     * 创建门店
     * @param shopModel
     * @return
     */
    ShopModel create(ShopModel shopModel) throws BaseException;

    /**
     * 查询门店
     * @param id
     * @return
     */
    ShopModel get(Integer id);

    /**
     * 门店列表
     * @return
     */
    List<ShopModel> selectAll();

    /**
     * 根据经纬度查询
     * @param longitude
     * @param latitude
     * @return
     */
    List<ShopModel> recommend(BigDecimal longitude, BigDecimal latitude);

    /**
     * 根据经纬度以及地理位置查询
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderby
     * @param categoryId
     * @param tags
     * @return
     */
    List<ShopModel> search(BigDecimal longitude, BigDecimal latitude, String keyword, Integer orderby, Integer categoryId, String tags);

    /**
     * 根据类目以及标签查询
     * @param keyword
     * @param categoryId
     * @param tags
     * @return
     */
    List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags);

    Integer countAllShop();

}
