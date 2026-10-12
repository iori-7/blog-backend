package com.itheima.exception;


import com.itheima.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.util.StringUtils;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value={Exception.class})
    public Result<String> handleException(Exception e){
        e.printStackTrace();
        return Result.error(StringUtils.hasLength(e.getMessage())?e.getMessage():"操作失败");
    }
}

