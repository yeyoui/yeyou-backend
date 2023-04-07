package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.dto.TeamQuery;
import com.yeyou.yeyoubackend.model.request.JoinTeamRequest;
import com.yeyou.yeyoubackend.model.request.TeamQuitRequest;
import com.yeyou.yeyoubackend.model.request.TeamUpdRequest;
import com.yeyou.yeyoubackend.model.vo.TeamUserPageVo;
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
     * @param userId
     * @return
     */
    long doAddTeam(Team team, long userId);

    /**
     * 显示所有指定的队伍
     * @param teamQuery
     * @param isAdmin
     * @param userId
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin,long userId);

    /**
     * 分页显示指定的队伍
     * @param teamQuery
     * @param isAdmin
     * @param userId 登录用户ID
     * @return
     */
    TeamUserPageVo pageTeams(TeamQuery teamQuery, boolean isAdmin,long userId);

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

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 用户删除队伍
     * @param teamId
     * @param loginUser
     * @return
     */
    boolean userDeleteTeam(long teamId, User loginUser);

    /**
     * 封装队伍信息
     * @param team
     * @param loginUserId 查询用户的id
     * @return
     */
    TeamUserVo packageTeamUserVo(Team team,long loginUserId);

    /**
     * 根据队伍ID查询（Redis）
     * @param teamId
     * @return
     */
    TeamUserVo getTeamsById(Long teamId,long userId);

    /**
     * 检查传入的参数
     * @param team
     * @param loginUser
     * @return
     */
    long checkNewTeamParam(Team team, User loginUser);
}
