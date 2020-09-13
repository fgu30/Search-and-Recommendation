package com.bin.spark.service.impl;


import com.bin.spark.mapper.UserModelMapper;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import tk.mybatis.mapper.entity.Example;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Created by 斌~
 * 2020/9/12 16:15
 * @author mac
 */
@Slf4j
@Service(value = "UserService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserModelMapper userModelMapper;

    @Override
    public UserModel getUser(Integer id) {
        UserModel userModel = userModelMapper.selectByPrimaryKey(id);
        userModel.setPassword("");
        return userModel;
    }

    /**
     * 注册
     * @param user 用户信息
     * @return UserModel
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserModel register(UserModel user) {
       //校验手机号不能重复
        Example example = new Example(UserModel.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("telephone", user.getTelephone());
        List<UserModel> userModels = userModelMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(userModels)){
            throw new RuntimeException("该手机号已经注册");
        }
        //注册保存用户信息
        user.setUpdatedAt(new Date());
        user.setUpdatedAt(new Date());
        //MD5摘要算法（Spring 自带）
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        int i = userModelMapper.insertSelective(user);
        return getUser(user.getId());
    }

    /**
     * 登录
     *
     * @param user 登录查询
     * @return
     */
    @Override
    public UserModel login(UserModel user) {

        //MD5摘要算法（Spring 自带）
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        //查询数据库
        return userModelMapper.queryByPhoneAndPassword(user.getTelephone(),user.getPassword());
    }

    /**
     * 查询用户总数
     *
     * @return
     */
    @Override
    public Integer countAllUser() {
        UserModel userModel = new UserModel();
        List<UserModel> userModelList = userModelMapper.select(userModel);
        return userModelList.size();
    }
}
