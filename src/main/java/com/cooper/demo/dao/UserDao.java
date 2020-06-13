package com.cooper.demo.dao;

import com.cooper.demo.Bean.User;
import org.apache.ibatis.annotations.*;

import java.util.Collection;

@Mapper
public interface UserDao {

    //@Insert("insert into user(id,username,password,role) values(#{id},#{username},#{password},#{role})")
    public void insertUser(User user);//注册用户

    //@Select("select * from user where id = #{id}")
    public User getUserByName(String username);//根据用户昵称寻找用户

    //@Update("update user set username=#{username},password=#{password},role=#{role} where id = #{id}")
    public void activeUser(User user);//激活用户信息

    //@Delete("delete from user where id = #{id}")
    public void deleteUser(Integer id);//删除用户

    public User selectUserByActiveCode(String activeCode);//根据激活码查找用户

    public User findUserByEmail(String email);//根据邮箱寻找用户

    public void updatePassword(User user); //重置密码后更新用户密码

    public void updateSecurity(User user);

    public boolean CheckUserName(String username);//检查用户名是否已经存在

    public Collection<User> getAllUser();//查找所有员工

    public void updateUserInfo(User user);//更新用户信息

    public void deleteUser(String username);//删除用户

    public void setStorageSpace (User user,Integer space);//设置用户存储容量

    public void setMaxStorageNumber(User user,Integer number);//设置用户的存储最大数量

    public void updateUserInfoByself(User user);//用户自己更新

}
