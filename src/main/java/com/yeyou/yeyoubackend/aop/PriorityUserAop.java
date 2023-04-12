package com.yeyou.yeyoubackend.aop;

import com.yeyou.yeyoubackend.annotation.PriorityUser;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoubackend.utils.UserHold;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


/**
 * 为优先度高的用户进行切面操作
 */
@Aspect
@Component
public class PriorityUserAop {

    private static ArrayList<Long> list=new ArrayList<>();
    @Resource
    private UserService userService;
    @Resource
    private StringRedisCacheUtils redisCacheUtils;
    /**
     * 为特权用户获取匹配用户缓存
     */
    @Around("@annotation(priorityUser)")
    public Object doPriorityMathCache(ProceedingJoinPoint joinPoint, PriorityUser priorityUser) throws Throwable{
        boolean useCache=false;
        int[] roles= priorityUser.anyRole();
        Integer userRole = UserHold.get().getUserRole();
        //特定角色
        if(priorityUser.mustRole()!=-1){
            if(priorityUser.mustRole()==userRole) useCache=true;
        }
        //从特定用户缓存中获取数据
        if(roles[0]!=-1){
            for (int role : roles) {
                if(role==userRole){
                    useCache=true;
                    break;
                }
            }
        }
        if(!useCache) return joinPoint.proceed();
        //从Redis缓存中直接读取
        User user = UserHold.get();
        String redisKey=RedisConstant.USER_MATCH_KEY+user.getId();
        return redisCacheUtils.queryWithLogicalExpireNoParam(redisKey,
                user.getId().toString(),
                RedisConstant.USER_MATCH_LOCK+user.getId(),
                ()->userService.cacheMathUsers(10,user),
                RedisConstant.USER_MATCH_TTL,
                TimeUnit.HOURS);
    }
}
