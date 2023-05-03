package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.Post;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.PostFavourService;
import com.yeyou.yeyoubackend.mapper.PostFavourMapper;
import com.yeyou.yeyoubackend.service.PostService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author lhy
 * @description 针对表【post_favour(帖子收藏表)】的数据库操作Service实现
 * @createDate 2023-04-27 15:39:43
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;

    @Override
    public int doPostFavour(long postId, User user) {
        //查找帖子是否存在
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        Long uid = user.getId();
        //通过代理类执行事务操作
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        //加锁
        synchronized (String.valueOf(uid).intern()) {
            return postFavourService.doPostFavourInner(postId, uid);
        }
    }



    @Override
    public int doPostFavourInner(long postId, long uid) {
        //1.查找收藏记录信息
        PostFavour postFavour = new PostFavour();
        postFavour.setPostId(postId);
        postFavour.setUserId(uid);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour queryPost = this.getOne(postFavourQueryWrapper);
        boolean result;
        //已收藏
        if (queryPost != null) {
            //删除收藏记录
            result = this.remove(postFavourQueryWrapper);
            if (result) {
                //同步
                result = postService.update()
                        .eq("id", postId)
                        .gt("favourNum", 0)
                        .setSql("favourNum = favourNum -1")
                        .update();
                return result?1:-1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            //未收藏
            result = this.save(postFavour);
            if (result) {
                //同步
                result = postService.update()
                        .eq("id", postId)
                        .setSql("favourNum = favourNum +1")
                        .update();
                return result?1:-1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }

    @Override
    public Page<Post> listFavourListByPage(IPage<Post> postPage, Wrapper<Post> queryWrapper, long favourUserId) {
        if(favourUserId<=0){
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(postPage,queryWrapper,favourUserId);
    }
}




