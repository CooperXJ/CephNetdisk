package com.cooper.demo.Chat.Fileter;

import com.cooper.demo.common.JWT;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * filterName 给过滤器取一个外号
 * urlPatterns 配置过滤器的拦截地址
 */
@WebFilter(urlPatterns = {"/chatWidget.html"})
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        Cookie[] cookies = request.getCookies();
        String token = "";
        if(cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("token")) {
                    token = cookie.getValue();
                }
            }
        }
        if(JWT.parseToken(token)==null){//重定向
            response.sendRedirect(request.getContextPath()+"/index.html");
        }else {
            filterChain.doFilter(request, response);
        }
    }


}
