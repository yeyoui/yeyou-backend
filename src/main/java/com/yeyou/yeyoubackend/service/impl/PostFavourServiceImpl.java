package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.PostFavour;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.PostFavourService;
import com.yeyou.yeyoubackend.mapper.PostFavourMapper;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoucommon.model.domain.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author lhy
 * @description 针对表【post_favour(帖子收藏表)】的数据库操作Service实现
 * @createDate 2023-04-27 15:39:43
 */
@Service
@Slf4j
//todo 加入Redis缓存，提高处理效率
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;
    @Resource
    private StringRedisTemplate sRedisTemplate;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;
    @Override
    public int doPostFavour(long postId, User user) {
        //查找帖子是否存在
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        Long uid = user.getId();

        return doPostFavourInner(postId, uid);
    }



    @Override
    public int doPostFavourInner(long postId, long uid) {
        //1.查找点赞记录信息
        Integer favourNum = stringRedisCacheUtils.queryWithLock(RedisConstant.POST_FAVOR_KEY, postId, Integer.class,
                RedisConstant.POST_FAVOR_LOCK, postService::getPostFavourNum, RedisConstant.CACHE_FIVE_TTL, TimeUnit.HOURS);
        //帖子不存在
        if(favourNum==null){
            return -1;
        }
        //帖子存在
        //1.查找收藏记录信息
        PostFavour postFavour = new PostFavour();
        postFavour.setPostId(postId);
        postFavour.setUserId(uid);
        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour queryPost = this.getOne(postFavourQueryWrapper);

        boolean result;
        //已收藏
        String key=RedisConstant.POST_FAVOR_KEY+postId;
        if (queryPost != null) {
            //删除收藏记录
            result = this.remove(postFavourQueryWrapper);
            if (result) {
                //同步
                sRedisTemplate.opsForValue().increment(key, -1);
                return 1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            //未收藏
            result = this.save(postFavour);
            if (result) {
                //同步
                sRedisTemplate.opsForValue().increment(key, 1);
                return 1;
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




