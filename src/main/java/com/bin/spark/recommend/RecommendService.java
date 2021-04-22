package com.bin.spark.recommend;

import com.bin.spark.mapper.RecommendDOMapper;
import com.bin.spark.model.RecommendDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 斌~
 * @version 1.0
 * @date 2021/4/22 下午11:33
 */
@Service
public class RecommendService implements Serializable {
    @Autowired
    RecommendDOMapper recommendDOMapper;

    /**
     * 根据用户id召回shopList
     * @param userId 用户Id
     * @return
     */
    public List<Integer> recall(Integer userId){
        RecommendDO recommendDO = recommendDOMapper.selectByPrimaryKey(userId);
        String[] shopIdArr = recommendDO.getRecommend().split(",");
        List<Integer> shopIdList = new ArrayList<>();
        for (String shopIdAtr : shopIdArr) {
            shopIdList.add(Integer.valueOf(shopIdAtr));
        }
        return shopIdList;
    }
}
