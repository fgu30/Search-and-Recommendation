package com.bin.spark.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 斌~
 * @version 1.0
 * @date 2020/9/13 10:03 上午
 */
@Data
public class LoginForm {

    @NotBlank(message = "手机号不能为空")
    private String telephone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
