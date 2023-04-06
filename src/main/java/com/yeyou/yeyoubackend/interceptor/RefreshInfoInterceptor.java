package com.yeyou.yeyoubackend.interceptor;

import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.utils.UserHold;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.yeyou.yeyoubackend.contant.UserConstant.USER_LOGIN_STATE;

@Component
@Slf4j
public class RefreshInfoInterceptor implements HandlerInterceptor {
    @Value("${spring.session.timeout}")
    private long expireTime;
    private StringRedisTemplate redisTemplate;
    @Autowired
    public RefreshInfoInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RefreshInfoInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从Session获取用户信息(Redis实现)
        Object obj = request.getSession().getAttribute(USER_LOGIN_STATE);
        //未登录
        if(obj==null) UserHold.set(null);
        //将Session信息存入UserHold中，方便业务获取用户信息
        UserHold.set((User) obj);
        //更新Redis中登录的过期时间
        //该层拦截器不做登录校验
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清除ThreadLocal中的内容，预防ThreadLocal未初始化的并发问题
        UserHold.remove();
    }
}
