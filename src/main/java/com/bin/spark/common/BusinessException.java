package com.bin.spark.common;

/**
 * @author ζ~
 * @version 1.0
 * @date 2020/9/13 11:38 δΈε
 */
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(ResponseEnum resultEnum) {
        super(resultEnum.getDesc());

        this.code = resultEnum.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
