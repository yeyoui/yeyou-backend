package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.TeamSeckill;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.request.TeamSeckillDelRequest;
import com.yeyou.yeyoubackend.model.request.TeamSeckillRequest;

import javax.servlet.http.HttpServletRequest;

/**
* @author lhy
* @description 针对表【team_seckill(队伍抢夺表)】的数据库操作Service
* @createDate 2023-03-19 19:46:48
*/
public interface TeamSeckillService extends IService<TeamSeckill> {

    /**
     * 将队伍添加到争夺列表中
     * @param teamSeckillRequest
     * @param loginUser
     * @return
     */
    Long addSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser);
    /**
     * 更新争夺列表中的队伍
     * @param teamSeckillRequest
     * @param loginUser
     * @return
     */
    Long updateSeckill(TeamSeckillRequest teamSeckillRequest, User loginUser);

    /**
     * 取消争夺状态
     * @param teamSeckillDelRequest
     * @return
     */
    Boolean deleteSeckillByTeamId(TeamSeckillDelRequest teamSeckillDelRequest, User loginUser);

    /**
     * 新增操作的时候发现队伍存在争夺队列中，当做更新操作处理
     * @param teamSeckillRequest
     * @param teamId
     */
    void shiftToUpdateSeckill(TeamSeckillRequest teamSeckillRequest,Long teamId);
}
