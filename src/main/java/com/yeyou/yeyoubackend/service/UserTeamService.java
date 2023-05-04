package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoucommon.model.vo.UserVo;

import java.util.List;

/**
* @author lhy
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2023-03-15 20:33:02
*/
public interface UserTeamService extends IService<UserTeam> {
    List<UserVo> getUserVoListByTeamId(Long teamId);

    @Deprecated
    long countTeamUserByTeamId(Long teamId);
}
