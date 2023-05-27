package com.yeyou.yeyoubackend.service.impl.inner;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoucommon.model.vo.PostVO;
import com.yeyou.yeyoucommon.service.InnerPostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@DubboService
@Slf4j
public class InnerPostServiceImpl implements InnerPostService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private PostService postService;
    @Resource
    private StringRedisTemplate sRedisTemplate;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, String userToken) {
        String key = RedisConstant.USER_TOKEN_KEY + userToken;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        //无登录信息
        ThrowUtils.throwIf(entries.isEmpty(), ErrorCode.NO_AUTH, "未登录");
        User user = BeanUtil.fillBeanWithMap(entries, new User(), false);
        return postService.getPostVoPage(postPage, user);
    }

    @Override
    public List<Post> selectBatchIds(List<Long> postIdList) {
        return postService.query()
                .in("id", postIdList)
                .list()
                .stream().peek(post -> {
                    post.setThumbNum(stringRedisCacheUtils.queryWithLock(
                            RedisConstant.POST_THUMB_KEY,post.getId(),Integer.class,RedisConstant.POST_THUMB_LOCK,
                            postService::getPostThumbNum,RedisConstant.CACHE_FIVE_TTL, TimeUnit.HOURS));
                    //todo 收藏数
                }).collect(Collectors.toList());
    }


    @Override
    public Post getPostById(long id) {
        return postService.getById(id);
    }

}
