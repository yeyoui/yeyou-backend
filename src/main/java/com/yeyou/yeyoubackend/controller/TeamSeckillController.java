package com.yeyou.yeyoubackend.controller;

import com.yeyou.yeyoubackend.common.BaseResponse;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.common.ResultUtils;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.request.TeamSeckillDelRequest;
import com.yeyou.yeyoubackend.model.request.TeamSeckillRequest;
import com.yeyou.yeyoubackend.model.vo.TeamUserSeckillVo;
import com.yeyou.yeyoubackend.model.vo.TeamUserVo;
import com.yeyou.yeyoubackend.service.TeamSeckillService;
import com.yeyou.yeyoubackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000"})
@RequestMapping("/teamSeckill")
public class TeamSeckillController {
    @Resource
    private UserService userService;
    @Resource
    private TeamSeckillService teamSeckillService;

    /**
     * 将队伍添加到争夺队列中
     * @param teamSeckillRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody TeamSeckillRequest teamSeckillRequest, HttpServletRequest request){
        if(teamSeckillRequest ==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long teamId=teamSeckillService.addSeckill(teamSeckillRequest,loginUser);
        return ResultUtils.success(teamId);
    }
    @Deprecated
    @PostMapping("/update")
    public BaseResponse<Long> update(@RequestBody TeamSeckillRequest teamSeckillRequest, HttpServletRequest request){
        if(teamSeckillRequest ==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long teamId=teamSeckillService.updateSeckill(teamSeckillRequest,loginUser);
        return ResultUtils.success(teamId);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody TeamSeckillDelRequest teamSeckillDelRequest, HttpServletRequest request){
        if(teamSeckillDelRequest==null) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Boolean success=teamSeckillService.deleteSeckillByTeamId(teamSeckillDelRequest,loginUser);
        return ResultUtils.success(success);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserSeckillVo>> list(){
        List<TeamUserSeckillVo> list = teamSeckillService.listAll();
        return ResultUtils.success(list);
    }

    @GetMapping("/joinSeckillTeam")
    public BaseResponse joinSeckillTeam(Long teamId, HttpServletRequest request) {
        if (teamId == null || teamId < 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean success = teamSeckillService.joinSeckillTeam(teamId, loginUser);
        return success ? ResultUtils.success(success) : ResultUtils.error(ErrorCode.SECKILL_NORMAL_ERROR);
    }
}
