package com.yeyou.yeyoubackend.service;

import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisCache {
    @Resource
    private StringRedisCacheUtils redisCacheUtils;

    @Test
    public void TestCache1(){
        String key = "test";
        Integer id=1;
//        redisCacheUtils.setWithLogicalExpire(key +id,"lhy123",5, TimeUnit.SECONDS);
        String ret = redisCacheUtils.queryWithLogicalExpire(key, id, String.class, "lock", this::getVal, 10, TimeUnit.MINUTES);
        System.out.println(ret);
    }

    public String getVal(Integer i){
        return "yeyoui";
    }

    @Test
    public void TestCache2(){
        String key = "test";
        int id=1;
//        redisCacheUtils.set(key +id,"lhy123",10, TimeUnit.MINUTES);
        String ret = redisCacheUtils.queryWithLogicalExpire(key, id, String.class, "lock", this::getVal, 10, TimeUnit.MINUTES);
        System.out.println(ret);
    }

}
