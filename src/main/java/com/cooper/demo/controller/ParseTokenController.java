package com.cooper.demo.controller;

import com.cooper.demo.Bean.ChatUser;
import com.cooper.demo.Bean.User;
import com.cooper.demo.service.Chat.UserService;
import com.cooper.demo.common.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParseTokenController {
    @Autowired
    UserService userService;

//    @Autowired
//    MapperService mapperService;

    /**
     * 解析token
     * */
    @RequestMapping("api/parseToken")
    public User parseToken(@RequestParam("token") String token){
        //解析token
        String username = JWT.parseToken(token);
        //并且返回对象信息
        return userService.queryUserInfo(username);
    }
}
