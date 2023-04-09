package com.yeyou.yeyoubackend.interceptor;

import com.yeyou.yeyoubackend.common.ErrorCode;
import com.yeyou.yeyoubackend.exception.BusinessException;
import com.yeyou.yeyoubackend.model.domain.User;
import com.yeyou.yeyoubackend.utils.UserHold;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equalsIgnoreCase("OPTIONS")){
            return true;//通过所有OPTION请求
        }
        User user = UserHold.get();
        if(user==null) throw new BusinessException(ErrorCode.NOT_LOGIN);
        return true;
    }
}
