package com.bin.spark.service;

import com.bin.spark.model.SellerModel;

import java.util.List;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:20 下午
 */
public interface SellerService {
    /**
     * 添加商户
     * @param sellerModel
     * @return
     */
    SellerModel create(SellerModel sellerModel);

    /**
     * 查询商户
     * @param id
     * @return
     */
    SellerModel get(Integer id);

    /**
     * 商户列表
     * @return
     */
    List<SellerModel> selectAll();

    /**
     * 生效/失效
     * @param id
     * @param disabledFlag
     * @return
     */
    SellerModel changeStatus(Integer id,Integer disabledFlag);

    Integer countAllSeller();
}
