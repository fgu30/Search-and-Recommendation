package com.bin.spark.service;

import com.bin.spark.model.UserModel;
import org.apache.ibatis.annotations.Param;

/**
 * Created by 斌~
 * 2020/9/12 15:59
 */
public interface UserService {
    UserModel getUser (@Param(value = "id") Integer id);
}
