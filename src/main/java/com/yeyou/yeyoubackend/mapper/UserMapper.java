package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeyou.yeyoubackend.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lhy
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-03-13 12:41:58
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




