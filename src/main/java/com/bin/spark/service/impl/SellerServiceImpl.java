package com.bin.spark.service.impl;

import com.bin.spark.model.SellerModel;
import com.bin.spark.service.SellerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:20 下午
 */
@Service
public class SellerServiceImpl implements SellerService {
    @Override
    public SellerModel create(SellerModel sellerModel) {
        return null;
    }

    @Override
    public SellerModel get(Integer id) {
        return null;
    }

    @Override
    public List<SellerModel> selectAll() {
        return null;
    }

    @Override
    public SellerModel changeStatus(Integer id, Integer disabledFlag)  {
        return null;
    }
}
