package com.yeyou.yeyoubackend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("businessException:{}",e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(InvalidFormatException.class)
    public BaseResponse runtimeExceptionHandler(InvalidFormatException e){
        log.error("runtimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"输入格式错误","输入格式错误");
    }
//    @ExceptionHandler(RuntimeException.class)
//    public BaseResponse runtimeExceptionHandler(RuntimeException e){
//        log.error("runtimeException",e);
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
//    }


}

