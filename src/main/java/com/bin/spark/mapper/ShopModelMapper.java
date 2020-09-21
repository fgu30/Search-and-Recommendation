package com.bin.spark.mapper;

import com.bin.spark.MyMapper;
import com.bin.spark.model.ShopModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mac
 */
@Repository
public interface ShopModelMapper extends MyMapper<ShopModel>{
    /**
     * 进入首页门店搜索
     * @param longitude
     * @param latitude
     * @return
     */
    List<ShopModel> recommend(@Param("longitude")BigDecimal longitude,@Param("latitude") BigDecimal latitude);

    /**
     * 搜索框🔍搜索
     * @param longitude
     * @param latitude
     * @param keyword
     * @param orderby
     * @param categoryId
     * @param tags
     * @return
     */
    List<ShopModel> search(@Param("longitude") BigDecimal longitude,
                           @Param("latitude") BigDecimal latitude,
                           @Param("keyword")String keyword,
                           @Param("orderby")Integer orderby,
                           @Param("categoryId")Integer categoryId,
                           @Param("tags")String tags);

    /**
     * 标签🔍搜索
     * @param keyword
     * @param categoryId
     * @param tags
     * @return
     */
    List<Map<String,Object>> searchGroupByTags(@Param("keyword")String keyword,
                                               @Param("categoryId")Integer categoryId,
                                               @Param("tags")String tags);


    List<Map<String,Object>> buildEsQuery(  @Param("sellerId")Integer sellerId,
                                            @Param("categoryId")Integer categoryId,
                                            @Param("shopId")Integer shopId);
}