package com.yeyou.yeyoubackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.dto.TeamQuery;
import com.yeyou.yeyoubackend.model.request.*;
import com.yeyou.yeyoubackend.model.vo.TeamUserPageVo;
import com.yeyou.yeyoubackend.model.vo.TeamUserVo;
import com.yeyou.yeyoubackend.service.TeamService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.service.UserTeamService;
import com.yeyou.yeyoubackend.utils.UserHold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/team")

public class TeamController {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest){
        if(teamAddRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = UserHold.get();
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long loginUserId = teamService.checkNewTeamParam(team, loginUser);
        long teamId = teamService.doAddTeam(team,loginUserId);
        return ResultUtils.success(teamId);
    }

//    @PostMapping("/delete")
//    public BaseResponse<Boolean> deleteByTeamId(@RequestBody long teamId){
//        if(teamId<0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        boolean result = teamService.removeById(teamId);
//        if(teamService.getById(teamId)==null) return ResultUtils.error(ErrorCode.PARAMS_ERROR,"不存在该ID");
//        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//        return ResultUtils.success(true);
//    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdRequest teamUpdRequest){
        if(teamUpdRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = UserHold.get();
        boolean result = teamService.updateTeam(teamUpdRequest,loginUser);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id,HttpServletRequest request){
        if(id<0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long loginUserId = userService.getLoginUser(request).getId();
        Team team = teamService.getById(id);
        if(team==null) throw new BusinessException(ErrorCode.NULL_ERROR);
        //数据脱敏（防止队伍信息暴露）
        if(!Objects.equals(loginUserId, team.getUserId())){
            team.setPassword("");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery){
        if(teamQuery==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = UserHold.get();
        boolean isAdmin = userService.isAdmin(loginUser);
        List<TeamUserVo> TeamUserVos = teamService.listTeams(teamQuery, isAdmin,loginUser.getId());
        return ResultUtils.success(TeamUserVos);
    }

    @GetMapping("/list/page")
    public BaseResponse<TeamUserPageVo> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = UserHold.get();
        boolean isAdmin = userService.isAdmin(loginUser);
        TeamUserPageVo TeamUserVos = teamService.pageTeams(teamQuery, isAdmin,loginUser.getId());
        return ResultUtils.success(TeamUserVos);
    }

    /**
     * 加入队伍
     * @param joinTeamRequest
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest joinTeamRequest,HttpServletRequest request){
        if(joinTeamRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(joinTeamRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param request
     * @return
     */
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest,HttpServletRequest request){
        if(teamQuitRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除队伍
     * @param teamDelRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDelRequest teamDelRequest, HttpServletRequest request) {
        if (teamDelRequest == null || teamDelRequest.getTeamId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long teamId = teamDelRequest.getTeamId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.userDeleteTeam(teamId, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 查询用户自己创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long userID = userService.getLoginUser(request).getId();
        teamQuery.setUserId(userID);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true,userID);
        return ResultUtils.success(teamUserVos);
    }

    /**
     * 查询用户自己创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long userID = userService.getLoginUser(request).getId();
        //查询用户加入的队伍id
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userID);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        if(userTeams==null || userTeams.isEmpty())  return ResultUtils.success(Collections.emptyList());
        Map<Long, List<UserTeam>> userTeamCollect = userTeams.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> ids = new ArrayList<>(userTeamCollect.keySet());
        teamQuery.setIdList(ids);
        List<TeamUserVo> teamUserVos = teamService.listTeams(teamQuery, true,userID);
        return ResultUtils.success(teamUserVos);
    }

}
