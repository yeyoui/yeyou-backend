package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeyou.yeyoubackend.model.domain.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lhy
* @description 针对表【tag(标签)】的数据库操作Mapper
* @createDate 2023-03-13 14:00:02
* @Entity generator.domain.Tag
*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

}




