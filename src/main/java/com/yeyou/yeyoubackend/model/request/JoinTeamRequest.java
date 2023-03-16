package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class JoinTeamRequest implements Serializable {

    private static final long serialVersionUID = 3008070250676297828L;

    /**
     * 队伍ID
     */
    private Long id;

    /**
     * 密码
     */
    private String password;
}
