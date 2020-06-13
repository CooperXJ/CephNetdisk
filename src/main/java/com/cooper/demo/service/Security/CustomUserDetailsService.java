package com.cooper.demo.service.Security;

import com.cooper.demo.Bean.User;
import com.cooper.demo.common.JWT;
import com.cooper.demo.service.User.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserServiceImpl userService;
    @Autowired
    HttpServletRequest request;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByName(username);

        if (user == null){
            throw new UsernameNotFoundException("用户名不存在!");
        }

        //建立session，将对象存储到session中以便调用当前用户的信息 将用户存起来
        HttpSession session = request.getSession();
        session.setAttribute("user",user);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if(user.getActive_status()==1)//首先对其注册状态进行检测
        {
            if(user.getUsername().equals("admin"))
            {
                authorities.add(new SimpleGrantedAuthority("ROLE_visitor"));
                authorities.add(new SimpleGrantedAuthority("ROLE_admin"));//这里必须要加上ROLE
                authorities.add(new SimpleGrantedAuthority("ROLE_VIP1"));//admin也可以进行用户的操作
            }
            else
            {
                authorities.add(new SimpleGrantedAuthority("ROLE_visitor"));
                authorities.add(new SimpleGrantedAuthority("ROLE_VIP1"));//这里必须要加上ROLE
            }
        }
        else
        {
            authorities.add(new SimpleGrantedAuthority("ROLE_visitor"));//这里必须要加上ROLE
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),authorities);
    }
}
