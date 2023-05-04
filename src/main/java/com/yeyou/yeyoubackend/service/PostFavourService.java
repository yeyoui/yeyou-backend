package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoucommon.model.domain.Post;

/**
* @author lhy
* @description 针对表【post_favour(帖子收藏表)】的数据库操作Service
* @createDate 2023-04-27 15:39:43
*/
public interface PostFavourService extends IService<PostFavour> {
    /**
     * 执行操作操作(入口)
     * @param postId
     * @param user
     * @return
     */
    int doPostFavour(long postId, User user);
    /**
     * 执行操作操作(内部实现)
     * @param postId
     * @param uid
     * @return
     */
    int doPostFavourInner(long postId, long uid);

    /**
     * 分页获取用户收藏的帖子列表
     *
     * @param postPage
     * @param queryWrapper
     * @param favourUserId
     * @return
     */
    Page<Post> listFavourListByPage(IPage<Post> postPage, Wrapper<Post> queryWrapper, long favourUserId);
}
