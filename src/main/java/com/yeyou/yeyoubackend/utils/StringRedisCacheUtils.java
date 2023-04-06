package com.yeyou.yeyoubackend.utils;

import com.google.gson.Gson;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.bo.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
@Slf4j
public class StringRedisCacheUtils{
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final Gson gson=new Gson();
    private static final ExecutorService threadPool= new ThreadPoolExecutor(2,4,
            5,TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20),
            new BasicThreadFactory.Builder().namingPattern("redisChache").build());

    public StringRedisCacheUtils(StringRedisTemplate redisTemplate,RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient=redissonClient;
    }

    //获取缓存
    /**
     * 失效时无阻塞的更新缓存
     * @param keyPrefix key前缀
     * @param type 返回值的类型
     * @param id id
     * @param dbCallback 如果缓存失效，获取最新值的回调（无参）
     * @param time 如果缓存失效，新的逻辑过期时间
     * @param timeUnit 时间单位
     * @return 结果
     * @param <R> 回调函数的结果类型
     */
    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbCallback,
                                       long time, TimeUnit timeUnit){
        String key=keyPrefix+id;
        //1.先查询缓存是否存在
        String json = redisTemplate.opsForValue().get(key);
        //2.缓存存在直接返回
        if(StringUtils.isNotBlank(json)){
            return gson.fromJson(json, type);
        }
        //3.缓存是空字符串，打印日志返回null
        if(json!=null){
            log.info("查询{}的缓存为空.",key);
            return null;
        }
        //4.缓存不存在，执行dbCallback获取数据
        R result=dbCallback.apply(id);
        if(result==null){
            //数据库无该数据(空字符串缓存5分钟)
            this.set(key,"", RedisConstant.CACHE_NULL_TTL,TimeUnit.SECONDS);
        }
        //5.将数据写入缓存
        this.set(key,gson.toJson(result), time,timeUnit);
        //6.返回结果
        return result;
    }
    /**
     * 带有锁的更新缓存(其他线程等待第一个获取锁的线程更新缓存，然后直接获取缓存返回)
     * @param keyPrefix key前缀
     * @param type 返回值的类型
     * @param id id
     * @param lockKey 加锁的对象名
     * @param dbCallback 如果缓存失效，获取最新值的回调（无参）
     * @param time 如果缓存失效，新的逻辑过期时间
     * @param timeUnit 时间单位
     * @return 结果
     * @param <R> 回调函数的结果类型
     */
    public <R,ID> R queryWithLock(String keyPrefix, ID id, Type type, String lockKey, Function<ID,R> dbCallback,
                                  long time, TimeUnit timeUnit){
        String key=keyPrefix+id;
        //1.先查询缓存是否存在
        String json = redisTemplate.opsForValue().get(key);
        //2.缓存存在直接返回
        if(StringUtils.isNotBlank(json)){
            return gson.fromJson(json, type);
        }
        //3.缓存是空字符串，打印日志返回null
        if(json!=null){
            log.info("查询{}的缓存为空.",key);
            return null;
        }
        //4.缓存不存在，执行dbCallback获取数据
        RLock rLock = redissonClient.getLock(lockKey);
        if(!rLock.tryLock()){
            try {
                while (!rLock.tryLock(5,-1,TimeUnit.SECONDS)){
                    ;//未获取到锁自旋等待
                }
                //解锁
                rLock.unlock();
                //之前的进程已经完成缓存更新
                return queryWithLock(keyPrefix,id,type,lockKey,dbCallback,time,timeUnit);
            } catch (InterruptedException e) {
                log.error("获取{}锁时出现中断->直接返回null",lockKey);
                return null;
            }
        }
        //当前进程获取到分布式锁了
        R result=dbCallback.apply(id);
        if(result==null){
            //数据库无该数据(空字符串缓存5分钟)
            this.set(key,"", RedisConstant.CACHE_NULL_TTL,TimeUnit.SECONDS);
        }
        //5.将数据写入缓存
        this.set(key,result, time,timeUnit);
        //解锁
        rLock.unlock();
        //6.返回结果
        return result;
    }
    /**
     * 逻辑过期（如果认定缓存过期，那么获取到锁的线程开启新线程更新缓存，并且返回旧值）
     * @param keyPrefix key前缀
     * @param id id
     * @param lockKey 加锁的对象名
     * @param dbCallback 如果缓存失效，获取最新值的回调（有参）
     * @param time 如果缓存失效，新的逻辑过期时间
     * @param timeUnit 时间单位
     * @return 结果
     * @param <R> 回调函数的结果类型
     */
    public <R,ID> R queryWithLogicalExpire(String keyPrefix, ID id,String lockKey, Function<ID,R> dbCallback,
        long time, TimeUnit timeUnit){
        String key=keyPrefix+id;
        //1.先查询缓存是否存在
        String json = redisTemplate.opsForValue().get(key);
        //2.缓存不存在直接返回null
        if(StringUtils.isBlank(json)){
            return null;
        }
        //3.检查缓存是否过期
        RedisData redisData = gson.fromJson(json, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        R ret= null;
        try {
            ret = (R) redisData.getData();
        } catch (Exception e) {
            log.error("redis中存储的类型与给定的类型不匹配",e);
            return null;
        }

        if(LocalDateTime.now().isBefore(expireTime)){
            //未过期可以返回
            return ret;
        }
        //4.缓存过期，执行dbCallback获取数据
        RLock rLock = redissonClient.getLock(lockKey);
        if(!rLock.tryLock()){
            //为获取到锁返回旧值
            return ret;
        }
        //当前进程获取到分布式锁了
        //开启新线程执行更新任务，本线程直接返回旧值
        threadPool.submit(()->{
            try {
                R result=dbCallback.apply(id);
                if(result==null){
                    //数据库无该数据(空字符串缓存5分钟)
                    this.set(key,"", RedisConstant.CACHE_NULL_TTL,TimeUnit.MINUTES);
                }
                //5.将数据写入缓存
                this.setWithLogicalExpire(key,result,time,timeUnit);
            } finally {
                rLock.unlock();
            }
        });
        //6.返回旧值
        return ret;
    }

    /**
     * 逻辑过期（如果认定缓存过期，那么获取到锁的线程开启新线程更新缓存，并且返回旧值）
     * @param keyPrefix key前缀
     * @param id id
     * @param lockKey 加锁的对象名
     * @param dbCallback 如果缓存失效，获取最新值的回调（无参）
     * @param time 如果缓存失效，新的逻辑过期时间
     * @param timeUnit 时间单位
     * @return 结果
     * @param <R> 回调函数的结果类型
     */
    public <R> R queryWithLogicalExpireNoParam(String keyPrefix, String id,String lockKey, Supplier<R> dbCallback,
                                           long time, TimeUnit timeUnit){
        String key=keyPrefix+id;
        //1.先查询缓存是否存在
        String json = redisTemplate.opsForValue().get(key);
        //2.缓存不存在直接返回null
        if(StringUtils.isBlank(json)){
            return null;
        }
        //3.检查缓存是否过期
        RedisData redisData = gson.fromJson(json, RedisData.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        R ret= null;
        try {
            ret = (R) redisData.getData();
        } catch (Exception e) {
            log.error("redis中存储的类型与给定的类型不匹配",e);
            return null;
        }

        if(LocalDateTime.now().isBefore(expireTime)){
            //未过期可以返回
            return ret;
        }
        //4.缓存过期，执行dbCallback获取数据
        RLock rLock = redissonClient.getLock(lockKey);
        if(!rLock.tryLock()){
            //为获取到锁返回旧值
            return ret;
        }
        //当前进程获取到分布式锁了
        //开启新线程执行更新任务，本线程直接返回旧值
        threadPool.submit(()->{
            try {
                R result=dbCallback.get();
                if(result==null){
                    //数据库无该数据(空字符串缓存5分钟)
                    this.set(key,"", RedisConstant.CACHE_NULL_TTL,TimeUnit.MINUTES);
                }
                //5.将数据写入缓存
                this.setWithLogicalExpire(key,result,time,timeUnit);
            } finally {
                rLock.unlock();
            }
        });
        //6.返回旧值
        return ret;
    }

    //设置带有过期时间的缓存
    public void set(String key, Object value, long time, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key, gson.toJson(value),time,timeUnit);
    }
    //设置无过期时间的缓存
    public void set(String key, Object value){
        redisTemplate.opsForValue().set(key, gson.toJson(value));
    }
    //设置逻辑过期缓存
    public void setWithLogicalExpire(String key, Object value, long time, TimeUnit timeUnit){
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        redisTemplate.opsForValue().set(key,gson.toJson(redisData));
    }

    public void removeCache(String ...key){
        redisTemplate.delete(Arrays.asList(key));
    }
}
