package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.mapper.UserTeamMapper;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.vo.UserVo;
import com.yeyou.yeyoubackend.service.UserTeamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
* @author lhy
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {
    @Resource
    UserTeamMapper userTeamMapper;

    @Override
    public List<UserVo> getUserVoListByTeamId(Long teamId) {
        return userTeamMapper.getUserVoListByTeamId(teamId);
    }

    @Override
    @Deprecated
    public long countTeamUserByTeamId(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return this.count(userTeamQueryWrapper);
    }
}




