package com.yeyou.yeyoubackend.exception;

import com.yeyou.yeyoubackend.common.ErrorCode;

/**
 * 抛异常工具类
 */
public class ThrowUtils {
    /**
     * 条件成立就抛出指定异常
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition,RuntimeException runtimeException){
        if(condition){
            throw  runtimeException;
        }
    }
    /**
     * 条件成立就抛出事务异常
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode){
        throwIf(condition,new BusinessException(errorCode));
    }
    /**
     * 条件成立就抛出事务异常(带有消息)
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode,String message){
        throwIf(condition,new BusinessException(errorCode,message));
    }
}
