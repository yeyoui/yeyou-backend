package com.yeyou.yeyoubackend.service;

import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoucommon.model.domain.Post;
import com.yeyou.yeyoucommon.service.InnerPostService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
public class PostTest {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PostService postService;
    @Resource
    private InnerPostService innerPostService;
    @Resource
    private PostThumbService postThumbService;
    @Resource
    private PostFavourService postFavourService;
    @Resource
    private UserService userService;


    @Test
    public void testGetPostThumbByIdAndCache(){
        long postId=1649691887614451716L;
        Post post = postService.query().select("thumbNum").eq("id", postId).one();
        if(post==null || post.getThumbNum()==null){
            log.error("post is null!!!");
            return;
        }
        String key = RedisConstant.POST_THUMB_KEY + postId;
        stringRedisTemplate.opsForValue().set(key,post.getThumbNum().toString());
        System.out.println(stringRedisTemplate.opsForValue().get(key));
    }

    @Test
    public void testThumbNumCache(){
        List<Post> posts = innerPostService.selectBatchIds(Arrays.asList(1649690183359762441L, 1649691887492816897L));
        posts.forEach(System.out::println);
    }
    @Test
    public void testDoThumb(){
        Long postId=1649690183359762434L;
        int ret = postThumbService.doPostThumb(postId, userService.getById(1L));
        System.out.println(ret);
        ret = postFavourService.doPostFavour(postId, userService.getById(1L));
        System.out.println(ret);
//        ret = postThumbService.doPostThumb(1649690183120687106L, userService.getById(1L));
//        System.out.println(ret);
//        ret = postFavourService.doPostFavour(postId, userService.getById(1L));
//        System.out.println(ret);
    }
    @Test
    public void testPostInfoSync(){
        List<Post> posts = postService.query().select("id","thumbNum","favourNum").list();
        posts.forEach(post -> {
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            Long postId = post.getId();
            boolean syncSucc;
            //同步点赞数
            String cacheThumbNum = ops.get(RedisConstant.POST_THUMB_KEY + postId);
            if(cacheThumbNum!=null && !cacheThumbNum.equals(post.getThumbNum().toString())){
                syncSucc=postService.update().set("thumbNum",Long.parseLong(cacheThumbNum)).eq("id", postId).update();
                if(!syncSucc){
                    log.error("帖子:{} 点赞数同步失败！",postId);
                }
            }
            //同步收藏数
            String cacheFavourNum = ops.get(RedisConstant.POST_FAVOR_KEY + postId);
            if(cacheFavourNum!=null && !cacheFavourNum.equals(post.getFavourNum().toString())){
                syncSucc = postService.update().set("favourNum", Long.parseLong(cacheFavourNum)).eq("id", postId).update();
                if(!syncSucc){
                    log.error("帖子:{} 收藏数同步失败！",postId);
                }
            }
        });
    }

}
