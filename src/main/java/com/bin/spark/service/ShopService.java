package com.bin.spark.service;

import com.bin.spark.common.BaseException;
import com.bin.spark.common.BusinessException;
import com.bin.spark.model.CategoryModel;
import com.bin.spark.model.ShopModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by 斌~
 * 2020/9/14 11:00
 * @author mac
 */
public interface ShopService {

    /**
     * 创建门店
     * @param shopModel
     * @return
     * @throws BaseException
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
     * 根据经纬度以及地理位置查询(mysql)
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderBy
     * @param categoryId
     * @param tags
     * @return
     */
    List<ShopModel> search(BigDecimal longitude, BigDecimal latitude, String keyword,
                           Integer orderBy, Integer categoryId, String tags);

    /**
     * 根据经纬度以及地理位置查询(elasticSearch)
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderBy
     * @param categoryId
     * @param tags
     * @return
     * @throws IOException
     */
    Map<String,Object> searchEs(BigDecimal longitude, BigDecimal latitude, String keyword,
                           Integer orderBy, Integer categoryId, String tags) throws IOException;

    /**
     * 根据类目以及标签查询
     * @param keyword
     * @param categoryId
     * @param tags
     * @return
     */
    List<Map<String, Object>> searchGroupByTags(String keyword, Integer categoryId, String tags);

    /**
     * 查询门店数量
     * @return
     */
    Integer countAllShop();

}
