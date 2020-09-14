package com.bin.spark.service.impl;

import com.bin.spark.common.BusinessException;
import com.bin.spark.common.ResponseEnum;
import com.bin.spark.mapper.SellerModelMapper;
import com.bin.spark.model.SellerModel;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:20 下午
 */
@Service
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerModelMapper sellerModelMapper;

    /**
     * 添加商户
     *
     * @param sellerModel
     * @return
     */
    @Override
    public SellerModel create(SellerModel sellerModel) {
        sellerModel.setCreatedAt(new Date());
        sellerModel.setUpdatedAt(new Date());
        sellerModel.setRemarkScore(BigDecimal.ZERO);
        sellerModel.setDisabledFlag(0);
        sellerModelMapper.insertSelective(sellerModel);
        return sellerModel;
    }

    /**
     * 查询商户
     *
     * @param id
     * @return
     */
    @Override
    public SellerModel get(Integer id) {
        return sellerModelMapper.selectByPrimaryKey(id);
    }

    /**
     * 商户列表
     *
     * @return
     */
    @Override
    public List<SellerModel> selectAll() {
        return sellerModelMapper.selectAll();
    }

    /**
     * 生效/失效
     *
     * @param id
     * @param disabledFlag
     * @return
     */
    @Override
    public SellerModel changeStatus(Integer id, Integer disabledFlag) {
        SellerModel sellerModel = get(id);
        if(sellerModel == null){
            throw new RuntimeException(ResponseEnum.PARAM_ERROR.getDesc());
        }
        sellerModel.setDisabledFlag(disabledFlag);
        sellerModelMapper.updateByPrimaryKeySelective(sellerModel);
        return sellerModel;
    }

    @Override
    public Integer countAllSeller() {
        SellerModel sellerModel = new SellerModel();
        return sellerModelMapper.selectCount(sellerModel);
    }

}
