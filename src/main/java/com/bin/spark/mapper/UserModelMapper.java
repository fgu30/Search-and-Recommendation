package com.bin.spark.mapper;

import com.bin.spark.MyMapper;
import com.bin.spark.model.UserModel;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
public interface UserModelMapper extends MyMapper<UserModel> {
    UserModel selectByUserId (Integer id);
}