package com.yeyou.yeyoubackend.service;

import com.yeyou.yeyoubackend.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class LearnIdGenerator {
    @Resource
    RedisIdWorker redisIdWorker;
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
    public void test() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task=()->{
            for (int j = 0; j < 100; j++) {
                long id = redisIdWorker.nextId("Order");
                System.out.println("id="+id);
            }
            latch.countDown();
        };

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            service.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = "+(end-begin));
    }

    @Test
    public void testCreateConsumerGroup(){
        System.out.println(redisTemplate.execute(TEAMSECKILL_SCRIPT, Collections.emptyList(), ""));
    }
}
