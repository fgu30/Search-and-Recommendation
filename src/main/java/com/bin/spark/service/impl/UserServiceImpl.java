package com.bin.spark.service.impl;

import com.bin.spark.mapper.UserModelMapper;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 斌~
 * 2020/9/12 16:15
 */
@Service(value = "UserService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserModelMapper userModelMapper;

    @Override
    public UserModel getUser(Integer id) {
        return userModelMapper.selectByPrimaryKey(id);
    }
}
