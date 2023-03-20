package com.yeyou.yeyoubackend.model.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
public class TeamUserSeckillVo extends TeamUserVo implements Serializable{
    private static final long serialVersionUID = 1248129066062476083L;
    /**
     * 本次可加入人数
     */
    private Integer joinNum;
}
