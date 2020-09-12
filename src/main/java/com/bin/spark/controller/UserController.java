package com.bin.spark.controller;

import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by 斌~@PathVariable
 * 2020/9/12 15:57
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public UserModel getUserInfo (@PathVariable Integer userId ){
       return userService.getUser(userId);
    }
}
