package com.yeyou.yeyoubackend.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.Collections;


@SpringBootTest
public class LuaExecute {
    @Resource
    RedisTemplate<String,String> redisTemplate;
    private static final DefaultRedisScript<Long> TEAMSECKILL_SCRIPT;
    //初始化
    static {
        TEAMSECKILL_SCRIPT=new DefaultRedisScript<>();
        TEAMSECKILL_SCRIPT.setLocation(new ClassPathResource("luaScript/CreateConsumerGroup.lua"));
        TEAMSECKILL_SCRIPT.setResultType(Long.class);
    }
    @Test
    public void addConsumerGroup(){
        redisTemplate.execute(
                TEAMSECKILL_SCRIPT,
                Collections.emptyList(),
                null);
    }

}
