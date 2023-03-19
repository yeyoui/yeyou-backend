package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍到争夺删除请求体
 *
 * @author yeoui
 */
@Data
public class TeamSeckillDelRequest implements Serializable {

    private static final long serialVersionUID = -5511049028750993895L;
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密   3.争夺
     */
    private Integer status;
}
