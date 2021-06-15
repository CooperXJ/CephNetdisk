package com.cooper.demo.service.Chat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cooper.demo.Bean.Friend;
import com.cooper.demo.Bean.ChatUser;
import com.cooper.demo.Bean.User;
import com.cooper.demo.Mapper.UserMapper;
import com.cooper.demo.common.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;

    private final String username_columnName = "username";
    public boolean checkIsExist(String username){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq(username_columnName,username);
        return userMapper.selectOne(wrapper)!=null;
    }

    public void registerUser(Map<String,Object> map){
        userMapper.registerUser(map);
    }

    public boolean login(HttpServletRequest request, HttpServletResponse response){
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String isRemeber = request.getParameter("isRemember");
        User user = userMapper.login(username,password);
        if(user!=null){
             String token = JWT.createToken(user.getUsername());
             Cookie cookie = new Cookie("token",token);
            if (isRemeber.equals("true")){
                cookie.setMaxAge(30*24*60*60);
            }
            cookie.setPath("/");
            response.addCookie(cookie);
            return true;
        }
        return false;
    }

    public List<Friend> getFriendsById(String user_id){
        return userMapper.getFriendsById(user_id);
    }

    public List<User> searchUser(String key){
        return userMapper.searchUser(key);
    }

    public void addFriend(Map<String,Object> map){
        userMapper.addFriend(map);
    }

    public List<Friend> getApplyFriend(String user_id){
        return userMapper.getApplyFriend(user_id);
    }

    public void acceptFriend(Map<String,Object> map){
        //修改isaccept
        userMapper.acceptFriend(String.valueOf(map.get("friend_id")),String.valueOf(map.get("user_id")));
        //并且插入接受的好友关系信息
        userMapper.addFriend(map);
    }

    public void changeHeader(String username,String header_img){
        userMapper.changeHeader(username,header_img);
    }

    public User queryUserInfo(String username){
        return userMapper.queryUserInfo(username);
    }
}
