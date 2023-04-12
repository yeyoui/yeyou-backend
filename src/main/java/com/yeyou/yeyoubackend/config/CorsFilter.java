package com.yeyou.yeyoubackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 过滤器解决跨域问题
 */
@Component
@Slf4j
public class CorsFilter implements Filter {

    private List<String> ALLOW_ORIGINS= Arrays.asList(
            "http://localhost:3000","http://47.113.148.209:3000"
    );

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

//        response.setHeader("Access-Control-Allow-Origin", "http://47.113.148.209:3000");
        setCrosHeader(request.getHeader("Origin"),response);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "content-type,Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        chain.doFilter(req, res);

    }
    //设置Access-Control-Allow-Origin
    private void setCrosHeader(String reqOrigin,HttpServletResponse response){
        if(reqOrigin==null) return;
        //匹配的地址才设置
        if (ALLOW_ORIGINS.contains(reqOrigin)) response.setHeader("Access-Control-Allow-Origin",reqOrigin);
        else log.error("host>>>>>>>>>{}",reqOrigin);
    }


}
