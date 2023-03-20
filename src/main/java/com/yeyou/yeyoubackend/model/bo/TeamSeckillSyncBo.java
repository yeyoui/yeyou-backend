package com.yeyou.yeyoubackend.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeamSeckillSyncBo implements Serializable {

    private static final long serialVersionUID = 7858033544031785746L;
    private Long orderId;
    private Long userId;
    private Long teamId;
}
