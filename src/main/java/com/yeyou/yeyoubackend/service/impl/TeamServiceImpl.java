package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.mapper.TeamMapper;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoubackend.model.dto.TeamQuery;
import com.yeyou.yeyoubackend.model.enums.TeamStatusEnum;
import com.yeyou.yeyoubackend.model.request.JoinTeamRequest;
import com.yeyou.yeyoubackend.model.request.TeamQuitRequest;
import com.yeyou.yeyoubackend.model.request.TeamUpdRequest;
import com.yeyou.yeyoubackend.model.vo.TeamUserVo;
import com.yeyou.yeyoubackend.model.vo.UserVo;
import com.yeyou.yeyoubackend.service.TeamService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author lhy
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-03-15 20:51:32
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;
    private Long teamId;

    @Override
    public long addTeam(Team team, User loginUser) {
        final long userId=loginUser.getId();
        //0.请求参数为空
        if(team==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1.队伍名称必须存在且小于20
        if(team.getName()==null || team.getName().length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名错误");
        }
        //2.描述如果存在必须小于512
        if(team.getDescription()!=null && team.getDescription().length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述过长");
        }
        //3.过期时间不能超过当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"过期时间设置错误");
        }
        //4.status如果设置为密码，密码不超过20位，Status默认为0
        int status = Optional.of(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(status);
        if(statusEnum.equals(TeamStatusEnum.SECRET) && (team.getPassword()==null || team.getPassword().length()>20)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置错误");
        }
        //如果类型不为密码，清空密码
        if(!statusEnum.equals(TeamStatusEnum.SECRET)){
            team.setPassword(null);
        }
        //5.最大人数不超过20
        int maxMemberNum = Optional.of(team.getMaxNum()).orElse(0);
        if(maxMemberNum>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //6.一个用户最多创建5个队伍
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId",userId);
        long count = this.count(teamQueryWrapper);
        if(count>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //7.更新队伍表 and 更新队伍关系表
        team.setUserId(userId);
        //队长就是创建者
        team.setLeaderId(userId);
        team.setId(null);
        boolean result = this.save(team);
        if(!result || team.getId()==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        teamId = team.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if(!result || userTeam.getId()==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //根据给定的条件查询
        if(teamQuery!=null){
            //1. 通过id查询
            Long teamId = teamQuery.getId();
            queryWrapper.eq((teamId != null && teamId >= 0), "id", teamId);
            //2. 通过id列表查询（查询多个id）
            List<Long> idList = teamQuery.getIdList();
            queryWrapper.in((idList != null && !idList.isEmpty()), "id", idList);
            //3. 通过名称或描述查询
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like("description",searchText).or().like("name",searchText));
            }
            //4. 通过名称查询
            String name = teamQuery.getName();
            queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
            //5. 通过描述查询
            String description = teamQuery.getDescription();
            queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
            //6. 通过最大人数查询
            Integer maxNum = teamQuery.getMaxNum();
            queryWrapper.eq((maxNum != null), "maxNum", maxNum);
            //7. 通过创建者id查询
            Long userId = teamQuery.getUserId();
            queryWrapper.eq((userId != null), "userId", userId);
            //8. 通过状态查询(管理员不设置状态-->默认全查)
            Integer status = teamQuery.getStatus();
            if(status!=null){
                TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(status);
                statusEnum= statusEnum==null?TeamStatusEnum.PUBLIC:statusEnum;
                //普通用户不能查询私密队伍
                if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                    throw new BusinessException(ErrorCode.NO_AUTH);
                }
                queryWrapper.eq("status", statusEnum.getVal());
            }else if(!isAdmin)queryWrapper.ne("status",TeamStatusEnum.PRIVATE.getVal());//不能查询私有
            //9.通过队长id查询
            Long leaderId = teamQuery.getLeaderId();
            queryWrapper.eq((leaderId != null), "leaderId", leaderId);
        }
        //不展示过期队伍 select ... from team where expireTime is Null or expireTime > now()
        queryWrapper.and(qw -> qw.isNull("expireTime").or().gt("expireTime", new Date()));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVo> TeamUserVos =teamList.stream()
                .map(team -> {
                    TeamUserVo teamUserVo = new TeamUserVo();
                    UserVo userVo = new UserVo();
                    BeanUtils.copyProperties(team,teamUserVo);
                    //获取队长信息
                    Long userId=team.getUserId();
                    User user = userService.getById(userId);
                    //队长信息正确
                    if(user!=null){
                        //数据脱敏
                        BeanUtils.copyProperties(user,userVo);
                        teamUserVo.setCreateUser(userVo);
                    }
                    //获取成员信息
                    Long teamId = team.getId();
                    List<UserVo> memberList=null;
                    if(teamId!=0) memberList = userTeamService.getUserVoListByTeamId(teamId);
                    if(memberList!=null && !memberList.isEmpty()){
                        teamUserVo.setMemberList(memberList);
                    }
                    /**
                     * TODO 设置是否已加入
                     */
                    return teamUserVo;
                }).collect(Collectors.toList());
        return TeamUserVos;
    }

    @Override
    public boolean updateTeam(TeamUpdRequest teamUpdRequest, User loginUser) {
        if(teamUpdRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1. ID不为空或小于0
        Long teamId = teamUpdRequest.getId();
        if(teamId==null || teamId<0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //2. 队伍必须存在
        Team oldTeam = this.getById(teamId);
        if(oldTeam==null) throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        //3. 只有组长或者管理员才能修改
        if(!Objects.equals(oldTeam.getLeaderId(), loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 如果设置加密房间必须设置密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(oldTeam.getStatus());
        if(TeamStatusEnum.SECRET.equals(statusEnum) && teamUpdRequest.getPassword()==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须设置密码");
        }
        //5. 如果更改队长ID，则必须存在
        Long leaderId = teamUpdRequest.getLeaderId();
        if(leaderId!=null && userService.getById(leaderId)==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "给定的队长ID不存在");
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    @Transactional
    public boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser) {
        if(joinTeamRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1. ID不为空或小于0
        Long teamId = joinTeamRequest.getTeamId();
        Long userID = loginUser.getId();
        if(teamId==null || teamId<0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍ID有误");
        //2. 队伍必须存在
        Team joinTeam = this.getById(teamId);
        if(joinTeam==null) throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        //3. 队伍不能是私有的或者争夺状态
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusEnum(joinTeam.getStatus());
        if(TeamStatusEnum.PRIVATE.equals(statusEnum)){
            throw new BusinessException(ErrorCode.NO_AUTH, "禁止加入私有队伍");
        }
        if (TeamStatusEnum.SECKILL.equals(statusEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "请前往秒杀界面加入用户");
        }
        //4. 队伍不能过期
        Date expireTime = joinTeam.getExpireTime();
        if(expireTime!=null && joinTeam.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        //5. 如果队伍是加密的必须验证密码
        if(TeamStatusEnum.SECRET.equals(statusEnum)&& !joinTeam.getPassword().equals(joinTeamRequest.getPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        //6. 队伍不能满员
//        long teamHadMemberCount = userTeamService.countTeamUserByTeamId(teamId);
        Integer teamHadMemberCount = this.query().select("memberNum").eq("id", teamId).one().getMemberNum();
        if(teamHadMemberCount>=joinTeam.getMaxNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }
        //7. 不能重复加入队伍
        QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("teamId",teamId);
        teamQueryWrapper.eq("userId",userID);
        long count = userTeamService.count(teamQueryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
        }
        //8. 不能加入超过5个队伍
        teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("teamId",teamId);
        long userJoinTeamCount = userTeamService.count(teamQueryWrapper);
        if(userJoinTeamCount>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入5个队伍");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userID);
        userTeam.setTeamId(teamId);
        //更新队伍成员数量
        boolean result = this.updateTeamMemberCount(1, true, teamId);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return userTeamService.save(userTeam);
    }

    @Override
    public boolean updateTeamMemberCount(int num, boolean inc,long teamId) {
        String ops = inc ? "+" : "-";
        return this.update().setSql("memberNum=memberNum"+ops+num).eq("id",teamId).update();
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        //1. 校验请求参数
        Long teamId=null;
        if(teamQuitRequest==null || (teamId=teamQuitRequest.getTeamId())==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍ID为空");
        }
        //2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3. 校验是否加入队伍
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQW = new QueryWrapper<>();
        userTeamQW.eq("userId", userId).eq("teamID", teamId);
        long count = userTeamService.count(userTeamQW);
        if(count==0) throw new BusinessException(ErrorCode.NO_AUTH,"未加入队伍");
        //4. 校验队伍人数（只针对队伍表更新）
        if(team.getMemberNum()==1){
            // 3.1如果只有一个人就直接解散（队长）
            //删除队伍
            this.removeById(teamId);
        }else {
            //3.2队伍中至少有两人
            if(Objects.equals(team.getLeaderId(), userId)){
                //队长退出队伍（要将队伍转让给加入时间最长的用户)
                List<UserTeam> userTeamList = userTeamService.query()
                        .select("userId")
                        .eq("teamId", teamId)
                        .last("order by id asc limit 2").list();
                if(userTeamList==null || CollectionUtils.isEmpty(userTeamList)){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                Long newLeaderId = userTeamList.get(1).getUserId();
                //更新队伍信息(修改队长id并且队伍人数-1)
                boolean result = this.update().eq("id", teamId).set("leaderId", newLeaderId).setSql("memberNum=memberNum-1").update();
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍信息失败");
                }
            }else{
                //3.2.1成员退出(队伍成员-1)
                this.updateTeamMemberCount(1, false, teamId);
            }
        }
        //删除关系
        return userTeamService.remove(userTeamQW);
    }

    @Override
    public boolean userDeleteTeam(long teamId, User loginUser) {
        //1. 校验请求参数
        if(teamId<0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //2. 校验队伍是否存在
        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //3. 校验是否为队长
        Long userID = loginUser.getId();
        if(!Objects.equals(userID,team.getLeaderId())){
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        //4. 移除队伍的所有关系信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //5. 删除队伍
        return this.removeById(teamId);
    }

}




