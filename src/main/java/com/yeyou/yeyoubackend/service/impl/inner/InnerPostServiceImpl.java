package com.yeyou.yeyoubackend.service.impl.inner;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.exception.ThrowUtils;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoucommon.model.vo.PostVO;
import com.yeyou.yeyoucommon.service.InnerPostService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@DubboService
public class InnerPostServiceImpl implements InnerPostService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private PostService postService;

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, String userToken) {
        String key= RedisConstant.USER_TOKEN_KEY+userToken;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        //无登录信息
        ThrowUtils.throwIf(entries.isEmpty(), ErrorCode.NO_AUTH,"未登录");
        User user = BeanUtil.fillBeanWithMap(entries, new User(), false);
        return postService.getPostVoPage(postPage, user);
    }

    @Override
    public List<Post> selectBatchIds(List<Long> postIdList) {
        return postService.query().in("id", postIdList).list();
    }

    @Override
    public Post getPostById(long id) {
        return postService.getById(id);
    }

}
