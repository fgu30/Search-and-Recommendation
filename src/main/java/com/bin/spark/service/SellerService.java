package com.bin.spark.service;

import com.bin.spark.model.SellerModel;

import java.util.List;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:20 下午
 */
public interface SellerService {
    SellerModel create(SellerModel sellerModel);
    SellerModel get(Integer id);
    List<SellerModel> selectAll();
    SellerModel changeStatus(Integer id,Integer disabledFlag);
}
