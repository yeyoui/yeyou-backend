package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeyou.yeyoubackend.model.domain.Team;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lhy
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2023-03-15 20:51:32
* @Entity generator.domain.Team
*/
@Mapper
public interface TeamMapper extends BaseMapper<Team> {

}




