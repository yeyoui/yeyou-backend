package com.yeyou.yeyoubackend.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 利用Redis自增和时间戳生成订单ID
 */
@Component
public class RedisIdWorker {
    private static final long BEGIN_TIMESTAMP=1678211353L;
    private static final int COUNT_BITS=32;

    private final RedisTemplate<String,String> stringRedisTemplate;

    public RedisIdWorker(RedisTemplate<String, String> stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix){
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp =nowSecond-BEGIN_TIMESTAMP;
        //Redis用(每天的订单都重新计算)
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        @SuppressWarnings("ConstantConditions")
        long increment = stringRedisTemplate.opsForValue().increment("incr:" + keyPrefix + date);
        return timeStamp<<COUNT_BITS | increment;
    }

    public long nextInc(String keyPrefix){
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        //Redis用(每天的订单都重新计算)
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        @SuppressWarnings("ConstantConditions")
        long increment = stringRedisTemplate.opsForValue().increment("incr:" + keyPrefix + date);
        return increment;
    }
}
