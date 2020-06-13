package com.cooper.demo.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.cooper.demo.Bean.Friend;
import com.cooper.demo.Bean.ChatUser;
import com.cooper.demo.Bean.User;
import com.cooper.demo.common.IDUtils;
import com.cooper.demo.service.Chat.UserService;
import com.cooper.demo.service.S3.S3Admin;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

@RestController
public class UserCtrl {

    @Autowired
    UserService userService;

    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    private S3Admin s3Admin;

    @Autowired
    S3ServiceImpl s3Service;

    @RequestMapping("api/checkIsExist_Action")
    public boolean checkIsExist(@RequestParam("username")String username){
        return userService.checkIsExist(username);
    }
//    @RequestMapping("api/register_Action")
//    public void registerUser(@RequestParam Map<String,Object> map){
//        userService.registerUser(map);
//    }

    @RequestMapping("api/register_Action")
    public void registerUser(String username,String password,String email,String nick_name) throws SocketTimeoutException {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setActive_status(0);
        user.setActiveCode(IDUtils.getUUID());
        user.setNick_name(nick_name);
        //创建s3用户
        org.twonote.rgwadmin4j.model.User S3User = s3Admin.createS3User(username);
        user.setAccess_key(S3User.getS3Credentials().get(0).getAccessKey());
        user.setSecret_key(S3User.getS3Credentials().get(0).getSecretKey());

        user.setStorageSpace(1024*1024*6);
        user.setMaxStorageNumber(1000);

        //每个人创立之初都会分配6空间，其中有1G是用来恢复误删的文件的
        String accessKey = "98SRU6JLWGSPCLZ9UVR4";
        String secretKey = "cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe";
        String adminEndpoint = "http://192.168.43.112:1999/admin";
        RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey(accessKey).secretKey(secretKey).endpoint(adminEndpoint)
                .build();

        //创建recover区，专门用来恢复用户误删的文件
        AmazonS3 s3 = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
        Bucket recover = s3Service.createBucket(s3,"recover");

        //设置该桶的大小为1G
        rgwAdmin.setIndividualBucketQuota(username,recover.getName(),100,1024*1024*1);
        user.setRecoverBucket(recover.getName());

        userServiceImpl.add(user,true);
    }

    @RequestMapping("api/login_action")
    public boolean login(HttpServletRequest request, HttpServletResponse response){
       return userService.login(request,response);
    }

    @RequestMapping("api/searchUser_action")
    public List<User> searchUser(@RequestParam("key") String key){
        return userService.searchUser(key);
    }

    @RequestMapping("api/getFriends_action")
    public List<Friend> getFriendsById(@RequestParam("user_id") String user_id){
        return userService.getFriendsById(user_id);
    }

    @RequestMapping("api/addFriend_action")
    public void addFriend(@RequestParam Map<String,Object> map){
        userService.addFriend(map);
    }

    @RequestMapping("api/getApplyFriend_action")
    public List<Friend> getApplyFriend(@RequestParam("friend_id") String friend_id){
        return userService.getApplyFriend(friend_id);
    }

    @RequestMapping("api/acceptFriend_action")
    public void acceptFriend(@RequestParam Map<String,Object> map){
        userService.acceptFriend(map);
    }

    @RequestMapping("api/changeHeader_action")
    public void changeHeader(@RequestParam("username") String username, @RequestParam("header_img") String header_img){
        userService.changeHeader(username,header_img);
    }

}
