package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

/**
 * 用户请求注册体
 */
@Data
public class UserRegisterRequest {
    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String code;
}
