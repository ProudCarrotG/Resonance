package com.resonance.exception;


import com.resonance.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器（Controller 的安检门）
 */
@RestControllerAdvice// 告诉 Spring：我要拦截所有 @RestController 抛出的异常
public class GlobalExceptionHandler {

    /*
        拦截运行错误(RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e){
        //打印到控制台，方便调试
        System.out.println("业务被异常拦截" + e.getMessage());

        //转换为标准的json格式返回

        return ApiResponse.fail(500,e.getMessage());
    }


    /*
        兜底拦截
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e){
        e.printStackTrace();

        return ApiResponse.fail(500,"系统开小差了，请稍后重试");
    }
}
