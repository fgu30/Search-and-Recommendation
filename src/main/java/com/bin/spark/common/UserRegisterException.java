package com.bin.spark.common;

import lombok.Getter;

/**
 * Created by 斌~
 * 2019/12/19 11:47
 * @author mac
 */
@Getter
public class UserRegisterException extends RuntimeException {

    private Integer code;

    public UserRegisterException(ResponseEnum resultEnum) {
        super(resultEnum.getDesc());

        this.code = resultEnum.getCode();
    }

    public UserRegisterException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
