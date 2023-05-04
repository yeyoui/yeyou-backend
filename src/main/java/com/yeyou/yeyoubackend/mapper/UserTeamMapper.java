package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeyou.yeyoubackend.model.domain.UserTeam;
import com.yeyou.yeyoucommon.model.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author lhy
* @description 针对表【user_team(用户队伍关系)】的数据库操作Mapper
* @createDate 2023-03-15 20:33:02
* @Entity generator.domain.UserTeam
*/
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {
    List<UserVo> getUserVoListByTeamId(Long teamId);
}




