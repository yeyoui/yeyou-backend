package com.yeyou.yeyoubackend;

import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class YeyoubackendApplicationTests {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testSearchUserByTags(){
        List<String> tags = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tags);
        Assertions.assertNotNull(userList);
        System.out.println(userList);
    }

    @Test
    public void testRedisTemplate(){
        redisTemplate.opsForValue().set("yeyou","lhy");
        Object yeyou = redisTemplate.opsForValue().get("yeyou");
        System.out.println((String) yeyou);
    }
}
