package com.yeyou.yeyoubackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.PostThumb;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.PostThumbService;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.mapper.PostThumbMapper;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoucommon.model.domain.Post;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
* @author lhy
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2023-04-27 15:39:43
*/
@Service
@Slf4j
//todo 加入Redis缓存，提高处理效率
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService{
    @Resource
    private PostService postService;
    @Resource
    private StringRedisTemplate sRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;

    @Override
    public int doPostThumb(long postId, User user) {
        //查找帖子是否存在
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        Long uid = user.getId();

        return doPostThumbInner(postId, uid);
    }

    @Override
    public int doPostThumbInner(long postId, long uid) {
        //1.查找点赞记录信息
        Integer thumbNum = stringRedisCacheUtils.queryWithLock(RedisConstant.POST_THUMB_KEY, postId, Integer.class,
                RedisConstant.POST_THUMB_LOCK, postService::getPostThumbNum, RedisConstant.CACHE_FIVE_TTL, TimeUnit.HOURS);
        //帖子不存在
        if(thumbNum==null){
            return -1;
        }
        //帖子存在
        PostThumb postThumb = new PostThumb();
        postThumb.setPostId(postId);
        postThumb.setUserId(uid);
        QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb queryPost = this.getOne(postThumbQueryWrapper);
        boolean result;
        //已点赞
        String key=RedisConstant.POST_THUMB_KEY+postId;
        if (queryPost != null) {
            //删除点赞记录
            result = this.remove(postThumbQueryWrapper);
            if (result) {
                //同步
                sRedisTemplate.opsForValue().increment(key, -1);
                return 1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } else {
            //未点赞
            result = this.save(postThumb);
            if (result) {
                //同步
                sRedisTemplate.opsForValue().increment(key, 1);
                return 1;
            }else{
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
    }
}




