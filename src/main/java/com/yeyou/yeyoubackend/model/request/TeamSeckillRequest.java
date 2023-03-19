package com.yeyou.yeyoubackend.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 增加队伍到争夺请求体
 *
 * @author yeoui
 */
@Data
public class TeamSeckillRequest implements Serializable {

    private static final long serialVersionUID = 1068601342707520760L;
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 本次可加入人数
     */
    private Integer joinNum;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;
}
