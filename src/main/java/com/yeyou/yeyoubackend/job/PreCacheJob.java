package com.yeyou.yeyoubackend.job;

import cn.hutool.core.util.RandomUtil;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.service.PostService;
import com.yeyou.yeyoubackend.service.UserService;
import com.yeyou.yeyoubackend.utils.StringRedisCacheUtils;
import com.yeyou.yeyoucommon.model.domain.Post;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yeyou.yeyoubackend.contant.RedisConstant.LOCK_SCHEDULE_MATCH_CACHE;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private StringRedisTemplate sRedisTemplate;
    @Resource
    private StringRedisCacheUtils stringRedisCacheUtils;
    @Resource
    private UserService userService;
    @Resource
    private PostService postService;
    @Resource
    private RedissonClient redissonClient;

    //一小时更新一次缓存
    @Scheduled(cron = "0 0 * * * *")
    public void doCacheMatchUser(){
        //给特权用户缓存数据
        RLock lock = redissonClient.getLock(LOCK_SCHEDULE_MATCH_CACHE);
        try {
            if(lock.tryLock(0,-1,TimeUnit.SECONDS)){
                List<Long> priorityUserList = userService.query().select("id").eq("userRole", 2).
                        list().stream().map(User::getId).collect(Collectors.toList());
                //给特权用户缓存数据
                for (Long userId : priorityUserList) {
                    User user = userService.getById(userId);
                    //随机时间，防止缓存雪崩
                    long expireTime = RedisConstant.USER_MATCH_TTL + RandomUtil.randomInt(0, 20);
                    List<User> userList = userService.cacheMathUsers(10, user);
                    String redisKey=RedisConstant.USER_MATCH_KEY+userId;
                    stringRedisCacheUtils.setWithLogicalExpire(redisKey,userList,
                            expireTime,TimeUnit.HOURS);
                }
            }
        } catch (InterruptedException e) {
            log.error("execute schedule error : ",e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                log.info("unlock the {}", LOCK_SCHEDULE_MATCH_CACHE);
                lock.unlock();
            }
        }
    }
    //一小时同步一次帖子缓存
    @Scheduled(cron = "0 0 * * * *")
    public void syncPostInfo(){
        List<Post> posts = postService.query().select("id","thumbNum","favourNum").list();
        posts.forEach(post -> {
            ValueOperations<String, String> ops = sRedisTemplate.opsForValue();
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
