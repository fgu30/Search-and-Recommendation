package com.bin.spark.controller;

import com.bin.spark.common.ResponseVo;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Created by 斌~
 * 2020/9/12 15:57
 * @author mac
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseVo<UserModel> getUserInfo (@PathVariable Integer userId ){
        UserModel user = userService.getUser(userId);
        if(user ==null){
            throw new RuntimeException("对象不存在");
        }
        return ResponseVo.success(user);
    }
}
