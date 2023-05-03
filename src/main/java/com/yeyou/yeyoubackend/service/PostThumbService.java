package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.model.domain.Post;
import com.yeyou.yeyoubackend.model.domain.PostThumb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.User;

/**
* @author lhy
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service
* @createDate 2023-04-27 15:39:43
*/
public interface PostThumbService extends IService<PostThumb> {
    /**
     * 执行操作操作(入口)
     * @param postId
     * @param user
     * @return
     */
    int doPostThumb(long postId, User user);
    /**
     * 执行操作操作(内部实现)
     * @param postId
     * @param uid
     * @return
     */
    int doPostThumbInner(long postId, long uid);
}
