package com.yeyou.yeyoubackend.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.vo.UserVo;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisCache {
//    @Resource
//    private StringRedisCacheUtils redisCacheUtils;
    @Resource
    RedisTemplate<String,Object> redisTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Test
    public void debugList(){
        Map<Object, Object> entries1 = redisTemplate.opsForHash().entries("user_token_key:4c39c594-003d-4c6d-8e16-5585f0618601");
        User user = BeanUtil.fillBeanWithMap(entries1, new User(), false);
        Gson gson = new Gson();
        stringRedisTemplate.opsForValue().set("user_token_key:233",gson.toJson(user));

//        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(user, new HashMap<>(),
//                CopyOptions.create().setIgnoreNullValue(true)
//                        .setFieldValueEditor((fieldName, fieldValue) -> {
//                            if(fieldValue==null) fieldValue = "";
//                            return fieldValue.toString();
//                        }));
//        String key="test....";
//        redisTemplate.opsForHash().putAll(key,stringObjectMap);
//        redisTemplate.expire(key, 10, TimeUnit.MINUTES);
//
//        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
//        User user1 = BeanUtil.fillBeanWithMap(entries, new User(), false);
//        System.out.println(user1);
    }

}
