package com.yeyou.yeyoubackend.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yeyou.yeyoubackend.contant.RedisConstant.LOCK_SCHEDULE_MATCH_CACHE;

@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    private List<Long> mainUserList= Arrays.asList(1L);

    @Test
    public void scheduleTest(){
        RLock lock = redissonClient.getLock(LOCK_SCHEDULE_MATCH_CACHE);
        try {
            if(lock.tryLock(0,-1,TimeUnit.SECONDS)){
                //给特权用户缓存数据
                for (Long userId : mainUserList) {
//                    Page<User> userPage = userService.page(new Page<>(1, 20));
//                    String redisKey=USER_RECOMMEND_KEY+userId;
//                    try {
//                        redisTemplate.opsForValue().set(redisKey,userPage,3000, TimeUnit.SECONDS);
//                    } catch (Exception e) {
//                        log.error("redis set key error ",e);
//                    }
                    Thread.sleep(9999999);
                }
            }
        } catch (InterruptedException e) {
            log.error("execute schedule error : ",e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                log.info("unlock the {}", LOCK_SCHEDULE_MATCH_CACHE);
                lock.unlock();
            }
        }
    }

}
