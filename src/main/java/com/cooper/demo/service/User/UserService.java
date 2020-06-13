package com.cooper.demo.service.User;

import com.cooper.demo.Bean.User;

import java.util.Collection;
import java.util.List;

public interface UserService {
    //用户注册
    void add(User user,boolean flag);

    //根据激活码查找用户
    User getUserByActiveCode(String activeCode);

    //激活后更新用户信息
    void activeUser(User user);

    //登录
    User get(User user);

    User findUserByMail(String mail) throws Exception;  //根据邮箱查找用户

    void updatePassword(User user); //重置密码后更新用户密码

    void updateSecurityCode(User user); //重置密码后删除验证码

    User getUserByName(String username);//根据用户名查找用户

    //查找所有的用户
    public List<User> getAllUser();

    //更新用户信息
    public void updateUserInfo(User user);

    //删除用户
    public void deleteUser(String username);

    //设置用户存储容量
    public void setStorageSpace(User user,Integer space);

    //用户自己更新自己的信息
    public void updateUserInfoByself(User user);

}
