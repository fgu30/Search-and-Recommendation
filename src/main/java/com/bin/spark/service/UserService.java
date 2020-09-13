package com.bin.spark.service;

import com.bin.spark.common.ResponseVo;
import com.bin.spark.model.UserModel;
import org.apache.ibatis.annotations.Param;

/**
 * Created by 斌~
 * 2020/9/12 15:59
 * @author mac
 */
public interface UserService {
    /**
     * 获取用户信息
     * @param id
     * @return
     */
    UserModel getUser (@Param(value = "id") Integer id);

    /**
     * 注册
     * @param user
     * @return
     */
    UserModel register(UserModel user);

    /**
     * 登录
     * @param userModel 登录查询
     * @return
     */
    UserModel login(UserModel userModel);

    /**
     * 查询用户总数
     * @return
     */
    Integer countAllUser();
}
