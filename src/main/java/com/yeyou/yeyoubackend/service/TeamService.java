package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.dto.TeamQuery;
import com.yeyou.yeyoubackend.model.request.JoinTeamRequest;
import com.yeyou.yeyoubackend.model.request.TeamUpdRequest;
import com.yeyou.yeyoubackend.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lhy
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-03-15 20:51:32
*/
public interface TeamService extends IService<Team> {

    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 显示所有指定的队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     * @param teamUpdRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdRequest teamUpdRequest, User loginUser);

    /**
     * 加入队伍
     * @param joinTeamRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser);

    /**
     * 更新队伍人数
     */
    boolean updateTeamMemberCount(int num,boolean inc,long teamId);
}
