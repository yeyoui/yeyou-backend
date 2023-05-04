package com.yeyou.yeyoubackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.yeyou.yeyoucommon.model.vo.UserVo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TeamUserVo implements Serializable {

    private static final long serialVersionUID = 4691143721993194445L;
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
     * 创建者ID
     */
    private Long userId;

    /**
     * 队长 id
     */
    private Long leaderId;

    /**
     * 成员数(默认1)
     */
    private Integer memberNum;

    /**
     * 0 - 公开，1 - 私有，2 - 加密   3.争夺
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队长信息
     */
    private UserVo createUser;

    /**
     * 成员信息
     */
    private List<UserVo> memberList;

    /**
     * 是否加入
     */
    private boolean hasJoin;
}
