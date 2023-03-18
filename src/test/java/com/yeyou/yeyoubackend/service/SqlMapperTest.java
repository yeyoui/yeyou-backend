package com.yeyou.yeyoubackend.service;

import com.yeyou.yeyoubackend.mapper.UserMapper;
import com.yeyou.yeyoubackend.mapper.UserTeamMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SqlMapperTest {
    @Resource
    UserTeamMapper userTeamMapper;
    @Resource
    UserMapper userMapper;
    @Test
    public void testGetUserList(){
        System.out.println(userTeamMapper.getUserVoListByTeamId(30L));
    }

    @Test
    public void testRandomUser(){
        userMapper.getRandomUser(10).forEach(System.out::println);
    }
}
