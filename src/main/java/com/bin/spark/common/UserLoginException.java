package com.bin.spark.common;

import lombok.Getter;

/**
 * Created by 斌~
 * 2019/12/19 11:47
 * @author mac
 */
@Getter
public class UserLoginException extends RuntimeException {
    private Integer code;

    public UserLoginException(ResponseEnum resultEnum) {
        super(resultEnum.getDesc());

        this.code = resultEnum.getCode();
    }

    public UserLoginException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
