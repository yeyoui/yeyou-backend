package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.PostThumb;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.PostThumbService;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.mapper.PostThumbMapper;
import com.yeyou.yeyoucommon.model.domain.Post;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author lhy
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2023-04-27 15:39:43
*/
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService{
    @Resource
    private PostService postService;

    @Override
    public int doPostThumb(long postId, User user) {
        //查找帖子是否存在
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        Long uid = user.getId();
        //通过代理类执行事务操作
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        //加锁
        synchronized (String.valueOf(uid).intern()) {
            return postThumbService.doPostThumbInner(postId, uid);
        }
    }



    @Override
    public int doPostThumbInner(long postId, long uid) {
        //1.查找点赞记录信息
        PostThumb postThumb = new PostThumb();
        postThumb.setPostId(postId);
        postThumb.setUserId(uid);
        QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb queryPost = this.getOne(postThumbQueryWrapper);
        boolean result;
        //已点赞
        if (queryPost != null) {
            //删除点赞记录
            result = this.remove(postThumbQueryWrapper);
            if (result) {
                //同步
                result = postService.update()
                        .eq("id", postId)
                        .gt("thumbNum", 0)
                        .setSql("thumbNum = thumbNum -1")
                        .update();
                return result?1:-1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            //未点赞
            result = this.save(postThumb);
            if (result) {
                //同步
                result = postService.update()
                        .eq("id", postId)
                        .setSql("thumbNum = thumbNum +1")
                        .update();
                return result?1:-1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}




