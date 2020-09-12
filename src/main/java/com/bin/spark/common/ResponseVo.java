package com.bin.spark.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.validation.BindingResult;

import java.util.Objects;

/**
 * created by 斌~ on 2019-12-11
 * JsonInclude  不返回空属性
 * @author mac
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ResponseVo<T> {

    private Integer status;

    private  String  msg;

    private T data;

    /**
     * 初始化一个新创建的 ResponseVo 对象
     * @param status 状态码
     * @param msg 返回内容
     */
    private ResponseVo(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    /**
     * 初始化一个新创建的 ResponseVo 对象
     * @param status 状态码
     * @param data 数据对象
     */
    private ResponseVo(Integer status, T data) {
        this.status = status;
        this.data = data;
    }


    /**
     * 初始化一个新创建的 ResponseVo 对象
     * @param status 状态码
     * @param data 数据对象
     */
    private ResponseVo(Integer status,String msg, T data) {
        this.status = status;
        this.msg =msg;
        this.data = data;
    }

    /**
     * 返回成功消息
     * @param msg 返回内容
     */
    public static <T> ResponseVo<T> successByMessage(String msg){
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(),msg);
    }

    /**
     * 返回成功消息
     * @param data 数据内容
     * @return 成功消息
     */
    public static <T> ResponseVo<T> success(T data){
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(),ResponseEnum.SUCCESS.name(),data);
    }

    /**
     * 返回错误消息
     * @param msg  返回内容
     * @return 错误消息
     */
    public static <T> ResponseVo<T> error(String msg){
        return new ResponseVo<>(ResponseEnum.ERROR.getCode(),msg);
    }

    /**
     * 根据消息体枚举返回成功消息
     * @return 成功消息
     */
    public static <T> ResponseVo<T> success(){
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(),ResponseEnum.SUCCESS.getDesc());
    }
    /**
     * 根据消息体枚举返回错误消息
     * @return 错误消息
     */
    public static <T> ResponseVo<T> error(ResponseEnum responseEnum){
        return new ResponseVo<>(responseEnum.getCode(),responseEnum.getDesc());
    }
    /**
     * 根据消息体枚举返回成功消息
     * @param msg 自定义错误消息
     * @return 成功消息
     */
    public static <T> ResponseVo<T> error(ResponseEnum responseEnum,String msg){
        return new ResponseVo<>(responseEnum.getCode(),msg);
    }

    /**
     * 返回错误信息
     * @return 错误消息
     */
    public static <T> ResponseVo<T> error(ResponseEnum responseEnum, BindingResult bindingResult){
        return new ResponseVo<>(responseEnum.getCode(),
                Objects.requireNonNull(bindingResult.getFieldError()).getField()+" "
        +bindingResult.getFieldError().getDefaultMessage());
    }

}
