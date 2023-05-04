package com.yeyou.yeyoubackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yeyou.yeyoucommon.model.domain.Post;
import org.apache.ibatis.annotations.Param;

/**
* @author lhy
* @description 针对表【post_favour(帖子收藏表)】的数据库操作Mapper
* @createDate 2023-04-27 15:39:43
* @Entity com.yeyou.yeyoubackend.model.domain.PostFavour
*/
public interface PostFavourMapper extends BaseMapper<PostFavour> {

    /**
     * 分页查询收藏帖子列表
     * @param postPage
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Post> listFavourPostByPage(IPage<Post> postPage, @Param(Constants.WRAPPER) Wrapper<Post> queryWrapper, long favourUserId);
}




