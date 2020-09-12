package com.bin.spark.common;

import lombok.Getter;

/**
 * created by 斌~ on 2019-12-11
 * @author mac
 */
@Getter
public enum ResponseEnum {
    /**
     * 异常错误枚举
     */
    ERROR(-1,"服务端错误"),

    SUCCESS(0,"成功"),

    PASSWORD_ERROR(1,"密码错误"),

    USER_EXIST(2,"用户已存在"),

    PARAM_ERROR(3,"参数错误"),

    NEED_LOGIN(10,"用户未登录，请先登录"),

    USERNAME_OR_PASSWORD_ERROR(11,"登录名或密码错误"),

    ;

     Integer code;

     String desc;

    ResponseEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
