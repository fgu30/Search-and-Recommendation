package com.bin.spark.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

import static com.bin.spark.common.ResponseEnum.ERROR;


/**
 * 统一异常处理
 * created by 斌~ on 2019-12-12
 * @author mac
 */
@ControllerAdvice
@Slf4j
public class RuntimeExceptionHandler {
    /**
     * @ResponseStatus(HttpStatus.FORBIDDEN) // 指定http错误码
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseVo<String> handle (RuntimeException e){
        log.info("error:{}",e.getMessage());
        return  ResponseVo.error(ERROR,e.getMessage());
    }



    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ResponseVo<String> handlerSellerException(BusinessException e) {
        return ResponseVo.error(ERROR, e.getMessage());
    }

    /**
     * 统一拦截  BindingResult 对象就不需要放在参数里边
     * 参数异常校验 @NOTNULLL
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseVo<String> notValidExceptionHandle(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Objects.requireNonNull(bindingResult.getFieldError());
        return ResponseVo.error(ResponseEnum.PARAM_ERROR,bindingResult.getFieldError().getField()
                + " " + bindingResult.getFieldError().getDefaultMessage());
    }
}
