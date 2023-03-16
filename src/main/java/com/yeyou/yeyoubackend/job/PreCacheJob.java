package com.yeyou.yeyoubackend.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yeyou.yeyoubackend.contant.RedisConstant.LOCK_SCHEDULE_RECOMMEND_CACHE;
import static com.yeyou.yeyoubackend.contant.RedisConstant.USER_RECOMMEND_KEY;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    private List<Long> mainUserList=Arrays.asList(1L);
    //每天18点34分预热用户列表缓存
    @Scheduled(cron = "0 34 18 * * *")
    public void doCacheRecommendUser(){
        //给特权用户缓存数据
        RLock lock = redissonClient.getLock(LOCK_SCHEDULE_RECOMMEND_CACHE);
        try {
            if(lock.tryLock(0,-1,TimeUnit.SECONDS)){
                //给特权用户缓存数据
                for (Long userId : mainUserList) {
                    Page<User> userPage = userService.page(new Page<>(1, 20));
                    String redisKey=USER_RECOMMEND_KEY+userId;
                    try {
                        redisTemplate.opsForValue().set(redisKey,userPage,3000, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error ",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("execute schedule error : ",e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                log.info("unlock the {}",LOCK_SCHEDULE_RECOMMEND_CACHE);
                lock.unlock();
            }
        }
    }
}
