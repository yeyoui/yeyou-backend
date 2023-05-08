package com.yeyou.yeyoubackend.config;

import com.yeyou.yeyoubackend.interceptor.LoginInterceptor;
import com.yeyou.yeyoubackend.interceptor.RefreshInfoInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //刷新Session请求
        registry.addInterceptor(new RefreshInfoInterceptor(redisTemplate)).addPathPatterns("/**").order(0);
        //检查是否登录
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/doc.html")
                .order(1);
    }
}
