package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.mapper.TeamSeckillMapper;
import com.yeyou.yeyoubackend.model.domain.Team;
import com.yeyou.yeyoubackend.model.domain.TeamSeckill;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.request.TeamSeckillDelRequest;
import com.yeyou.yeyoubackend.model.request.TeamSeckillRequest;
import com.yeyou.yeyoubackend.service.TeamSeckillService;
import com.yeyou.yeyoubackend.service.TeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
* @author lhy
* @description 针对表【team_seckill(队伍抢夺表)】的数据库操作Service实现
* @createDate 2023-03-19 19:46:48
*/
@Service
public class TeamSeckillServiceImpl extends ServiceImpl<TeamSeckillMapper, TeamSeckill>
    implements TeamSeckillService {
    @Resource
    private TeamService teamService;
    @Override
    @Transactional
    public Long addSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        Long teamId = checkTeamSeckillRequest(teamSeckillRequest, loginUser);
        //队伍不能再争夺队列
        Long count = this.query().eq("teamId", teamId).count();
        if(count!=0){
            //新增操作的时候发现队伍存在争夺队列中，当做更新操作处理
            shiftToUpdateSeckill(teamSeckillRequest,teamId);
            return teamId;
        }
        //新增队伍到争夺列表
        TeamSeckill teamSeckill = new TeamSeckill();
        BeanUtils.copyProperties(teamSeckillRequest,teamSeckill);
        boolean result = this.save(teamSeckill);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        //修改队伍状态为争夺状态
        result = teamService.update().set("status", 3).eq("id", teamId).update();
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return teamId;
    }

    @Override
    public Long updateSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        Long teamId = checkTeamSeckillRequest(teamSeckillRequest, loginUser);
        //队伍已必须在争夺队列
        TeamSeckill teamSeckill1 = this.query().select("id").eq("teamId", teamId).one();
        if(teamSeckill1==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在争夺队列中");
        //修改争夺列表的队伍
        TeamSeckill teamSeckill = new TeamSeckill();
        BeanUtils.copyProperties(teamSeckillRequest,teamSeckill);
        teamSeckill.setId(teamSeckill1.getId());
        boolean result = this.updateById(teamSeckill);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return teamId;
    }

    @Override
    public void shiftToUpdateSeckill(TeamSeckillRequest teamSeckillRequest,Long teamId){
        //修改争夺列表的队伍
        TeamSeckill teamSeckill = this.query().select("id").eq("teamId", teamId).one();
        BeanUtils.copyProperties(teamSeckillRequest,teamSeckill);
        teamSeckill.setId(teamSeckill.getId());
        boolean result = this.updateById(teamSeckill);
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
    }

    @Override
    public Boolean deleteSeckillByTeamId(TeamSeckillDelRequest teamSeckillDelRequest, User loginUser) {
        //1. 检查参数
        if(teamSeckillDelRequest==null || teamSeckillDelRequest.getTeamId()==null ||
        teamSeckillDelRequest.getStatus()==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        Long teamId = teamSeckillDelRequest.getTeamId();
        Integer status = teamSeckillDelRequest.getStatus();
        if(teamId<0 || status<0 ||status>3) throw new BusinessException(ErrorCode.PARAMS_ERROR,"信息输入错误");
        //2. 查询对应的队伍
        Team team = teamService.getById(teamId);
        TeamSeckill teamSeckill = this.query().select("id").eq("teamId", teamId).one();
        if(team==null || teamSeckill==null) throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");

        //3. 只有队长才能操作
        if(!Objects.equals(team.getLeaderId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 删除信息
        this.removeById(teamSeckill.getId());
        //5. 修改队伍状态为设定的状态
        boolean result = teamService.update().set("status", status).eq("id", teamId).update();
        if(!result) throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        return true;
    }

    private Long checkTeamSeckillRequest(TeamSeckillRequest teamSeckillRequest, User loginUser) {
        //1. 检查参数
        if(teamSeckillRequest ==null || teamSeckillRequest.getTeamId()<0 ||
                teamSeckillRequest.getJoinNum()==null || teamSeckillRequest.getJoinNum()<=0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        //1.1校验时间
        if(teamSeckillRequest.getBeginTime().after(teamSeckillRequest.getEndTime())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"结束时间不能早于开始时间");
        }
        if(teamSeckillRequest.getEndTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"结束时间不能早于当前时间");
        }
        Long teamId = teamSeckillRequest.getTeamId();
        Team team = teamService.getById(teamId);
        if(team==null) throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        //3. 只有队长才能操作
        if(!Objects.equals(team.getLeaderId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //4. 校验队伍剩余人数是否足够
        if(team.getMaxNum()-team.getMemberNum()< teamSeckillRequest.getJoinNum()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍空间不足");
        }
        return teamId;
    }
}




