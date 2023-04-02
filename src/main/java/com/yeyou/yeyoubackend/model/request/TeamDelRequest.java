package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍删除请求体
 *
 * @author yeyoui
 */
@Data
public class TeamDelRequest implements Serializable {

    private static final long serialVersionUID = 3191241716378120793L;

    /**
     * id
     */
    private Long teamId;

}
