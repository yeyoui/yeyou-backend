package com.yeyou.yeyoubackend.model.vo;

import com.yeyou.yeyoucommon.model.vo.UserVo;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ToString(callSuper = true)
public class TeamUserSeckillVo  implements Serializable{
    private static final long serialVersionUID = 1248129066062476083L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 成员数(默认1)
     */
    private Integer memberNum;

    /**
     * 0 - 公开，1 - 私有，2 - 加密   3.争夺
     */
    private Integer status;

    /**
     * 队长信息
     */
    private UserVo createUser;

    /**
     * 成员信息
     */
    private List<UserVo> memberList;
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
