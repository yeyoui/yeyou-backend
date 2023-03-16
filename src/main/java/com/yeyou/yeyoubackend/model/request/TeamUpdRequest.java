package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamUpdRequest implements Serializable {

    private static final long serialVersionUID = 4591674371242964378L;
    /**
     * 队伍ID
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队长ID
     */
    private Long leaderId;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
