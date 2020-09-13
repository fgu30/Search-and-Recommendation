package com.bin.spark.mapper;

import com.bin.spark.MyMapper;
import com.bin.spark.model.UserModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author mac
 */
@Repository
public interface UserModelMapper extends MyMapper<UserModel> {
    /**
     * 获取用户信息
     * @param id
     * @return
     */
    UserModel selectByUserId (Integer id);

    /**
     * 根据手机号和密码查询用户
     * @param telephone
     * @param password
     * @return
     */
    UserModel queryByPhoneAndPassword(@Param("telephone") String telephone, @Param("password") String password);
}