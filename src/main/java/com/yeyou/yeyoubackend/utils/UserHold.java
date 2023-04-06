package com.yeyou.yeyoubackend.utils;

import com.yeyou.yeyoubackend.model.domain.User;

/**
 * 使用ThreadLocal存储当前会话的用户信息
 */
public class UserHold {
    private static final ThreadLocal<User> userTl=new ThreadLocal<>();

    public static void set(User user){
        userTl.set(user);
    }

    public static User get(){
        return userTl.get();
    }

    public static void remove(){
        userTl.remove();
    }
}
