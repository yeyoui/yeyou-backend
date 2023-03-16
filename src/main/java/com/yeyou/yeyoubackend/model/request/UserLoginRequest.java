package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

/**
 * 用户请求登录体
 */
@Data
public class UserLoginRequest {
    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;
}
