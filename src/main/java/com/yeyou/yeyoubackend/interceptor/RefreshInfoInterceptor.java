package com.yeyou.yeyoubackend.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.gson.Gson;
import com.yeyou.yeyoubackend.contant.RedisConstant;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.utils.UserHold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

import static com.yeyou.yeyoubackend.contant.UserConstant.USER_LOGIN_STATE;

@Component
@Slf4j
public class RefreshInfoInterceptor implements HandlerInterceptor {
    @Value("${spring.session.timeout}")
    public long expireTime=1200;

    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    public RefreshInfoInterceptor(StringRedisTemplate redisTemplate) {
        this.stringRedisTemplate = redisTemplate;
    }

    public RefreshInfoInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从authorization获取用户信息(Redis实现)
//        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
        String token = request.getHeader("authorization");
        if(StringUtils.isBlank(token)){
            return true;
        }
        String key= RedisConstant.USER_TOKEN_KEY+token;
        String json = stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isBlank(json)){
            return true;
        }
        Gson gson = new Gson();
        User user = gson.fromJson(json, User.class);
        UserHold.set(user);
        //更新Redis中登录的过期时间
        stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        //该层拦截器不做登录校验
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清除ThreadLocal中的内容，预防ThreadLocal未初始化的并发问题
        UserHold.remove();
    }
}
