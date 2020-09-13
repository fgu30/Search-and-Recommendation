package com.bin.spark.controller;

import com.bin.spark.common.ResponseEnum;
import com.bin.spark.common.ResponseVo;
import com.bin.spark.common.UserLoginException;
import com.bin.spark.common.UserRegisterException;
import com.bin.spark.form.LoginForm;
import com.bin.spark.form.RegisterFrom;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import org.omg.CORBA.UserException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Created by 斌~
 * 2020/9/12 15:57
 * @author mac
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public ModelAndView test(){
        return new ModelAndView("/test.html");
    }

    @GetMapping("/{userId}")
    @ResponseBody
    public ResponseVo<UserModel> getUserInfo (@PathVariable Integer userId ){
        UserModel user = userService.getUser(userId);
        if(user ==null){
            throw new RuntimeException("对象不存在");
        }
        return ResponseVo.success(user);
    }


    @PostMapping("/register")
    @ResponseBody
    public ResponseVo<UserModel> register(@Valid @RequestBody RegisterFrom registerFrom,
                                          BindingResult bindingResult){
        //参数校验
        if(bindingResult.hasErrors()){
            throw new UserRegisterException(ResponseEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(registerFrom,userModel);
        return ResponseVo.success(userService.register(userModel));
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseVo<UserModel> login(@Valid @RequestBody LoginForm loginForm,
                                          BindingResult bindingResult){
        //参数校验
        if(bindingResult.hasErrors()){
            throw new UserLoginException(ResponseEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(loginForm,userModel);
        return userService.login(userModel);
    }
}
