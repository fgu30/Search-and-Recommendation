package com.bin.spark.controller;

import com.bin.spark.common.BusinessException;
import com.bin.spark.common.ResponseEnum;
import com.bin.spark.common.ResponseVo;
import com.bin.spark.form.LoginForm;
import com.bin.spark.form.RegisterFrom;
import com.bin.spark.model.UserModel;
import com.bin.spark.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

/**
 * Created by 斌~
 * 2020/9/12 15:57
 * @author mac
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    public static final String CURRENT_USER_SESSION = "currentUserSession";

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
            throw new BusinessException(ResponseEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(registerFrom,userModel);
        return ResponseVo.success(userService.register(userModel));
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseVo<UserModel> login(@Valid @RequestBody LoginForm loginForm,
                                          BindingResult bindingResult,HttpServletRequest httpServletRequest){
        //参数校验
        if(bindingResult.hasErrors()){
            throw new BusinessException(ResponseEnum.PARAM_ERROR.getCode(),
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(loginForm,userModel);
        UserModel userLogin =userService.login(userModel);;
        if(userLogin == null){
            return ResponseVo.error(ResponseEnum.PASSWORD_ERROR);
        }else{
            log.info("用户{}登录成功",userLogin.getNickName());
        }
        userLogin.setPassword("");
        httpServletRequest.getSession().setAttribute(CURRENT_USER_SESSION,userLogin);

        log.info(httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION).toString());
        return ResponseVo.success(userLogin);
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseVo<String> logout(HttpServletRequest httpServletRequest){
        httpServletRequest.getSession().invalidate();
        return ResponseVo.success("退出成功！");
    }
    @PostMapping("/getcurrentuser")
    @ResponseBody
    public ResponseVo<UserModel> getCurrentUser(HttpServletRequest httpServletRequest){
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute(CURRENT_USER_SESSION);
        return ResponseVo.success(userModel);
    }
}
