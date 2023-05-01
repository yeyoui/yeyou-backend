package com.yeyou.yeyoubackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.model.domain.Post;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.model.dto.post.PostQueryRequest;
import com.yeyou.yeyoubackend.model.vo.PostVO;

/**
* @author lhy
* @description 针对表【post(帖子)】的数据库操作Service
* @createDate 2023-04-27 11:46:48
*/
public interface PostService extends IService<Post> {

    /**
     * 校验参数是否正确
     * @param post
     * @param add
     */
    void validPost(Post post, boolean add);

    /**
     * 新增帖子
     * @param post
     * @param user
     * @return
     */
    long savePost(Post post, User user);

    /**
     * 更新帖子
     * @param post
     * @param user
     * @return
     */
    boolean updatePost(Post post, User user);

    /**
     * 封装帖子信息
     * @param post
     * @param user
     * @return
     */
    PostVO getPostVO(Post post, User user);

    /**
     * 分页查询
     * @param postQueryRequest
     * @param user
     * @return
     */
    Page<PostVO> listPostVoPage(PostQueryRequest postQueryRequest, User user);

    /**
     * 获取查询包装类
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 封装帖子信息
     * @param postPage
     * @param user
     * @return
     */
    Page<PostVO> getPostVoPage(Page<Post> postPage, User user);
}
