package com.yeyou.yeyoubackend.service;


import cn.hutool.core.util.RandomUtil;
import com.yeyou.yeyoubackend.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUser {
    @Resource
    private UserService userService;
    @Resource
    RedisTemplate<String,Object> redisTemplate;
    private ExecutorService executorService = new ThreadPoolExecutor(12, 20,
            1000, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    private static final int INSERT_NUM=200;
    @Test
    public void incrTest(){
        Long idIncr = redisTemplate.opsForValue().increment("IdIncr");
        System.out.println(idIncr);
    }
    /**
     * 批量插入用户(Mysql分批)
     */
    @Test
    public void doInsertUserAllByMysql(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        ArrayList<User> users = new ArrayList<>(INSERT_NUM);
        for (int i = 20; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假夜悠"+i);
            user.setUserAccount("fakeyuyoui"+i);
            user.setAvatarUrl(userService.randomUserIcon());
            user.setUserPassword("5f47bf575f99048f10453056cd6e1cff");
            user.setPhone("");
            user.setEmail(RandomUtil.randomNumbers(10)+"@qq.com");
//            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            Long uid= redisTemplate.opsForValue().increment("IdIncr");
            user.setUserCode(uid.toString());
            users.add(user);
        }
        userService.saveBatch(users,1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 使用并发来插入用户
     */
    @Test
    public void doConcurrencyInsertUser(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        ArrayList<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        final int batchSize=300;
        int cnt=0;
        for (int i = 0; i < INSERT_NUM/batchSize; i++) {
            ArrayList<User> users = new ArrayList<>(INSERT_NUM);
            while (true){
                User user = new User();
                user.setUsername("假夜悠"+i);
                user.setUserAccount("fakeyuyoui"+i);
                user.setAvatarUrl(userService.randomUserIcon());
                user.setUserPassword("12345678");
                user.setPhone("");
                user.setEmail(RandomUtil.randomNumbers(10)+"@qq.com");
//            user.setTags("[]");
                user.setUserStatus(0);
                user.setUserRole(0);
                Long uid= redisTemplate.opsForValue().increment("IdIncr");
                user.setUserCode(uid.toString());
                users.add(user);
                if(++cnt%batchSize==0) break;
            }
            CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
                System.out.println("Thread："+Thread.currentThread().getName());
                userService.saveBatch(users, batchSize);
            },executorService);
            completableFutures.add(future);
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}

