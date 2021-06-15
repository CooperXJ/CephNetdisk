package com.cooper.demo.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.cooper.demo.Bean.User;
import com.cooper.demo.common.IDUtils;
import com.cooper.demo.common.JWT;
import com.cooper.demo.common.SecurityCode;
import com.cooper.demo.service.S3.S3Admin;
import com.cooper.demo.service.S3.S3Service;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class IndexController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private S3Admin s3Admin;
    @Autowired
    private S3ServiceImpl s3Service;

    @RequestMapping(value = {"/","/UserLogin","/loginPage"})//将数据保存到model中，传给register
    public ModelAndView login()
    {

        ModelAndView modelAndView = new ModelAndView("login.html");
        return modelAndView;
    }

    @PostMapping(value = {"/","/UserLogin","/loginPage"})
    @ResponseBody
    public String checkUsername(String username,String pwd,HttpServletResponse response)
    {
        String msg = "";
        if(username!=null)
        {
            if(userService.checkUserName(username))
            {

                //创建cookie，方便chat时调用
                String token = JWT.createToken(username);
                Cookie cookie = new Cookie("token",token);
                cookie.setPath("/");
                response.addCookie(cookie);
                msg = "ok";
            }
            else
            {
                msg = "用户名错误";
            }
        }
        return msg;
    }

    @PostMapping("checkPassword")
    @ResponseBody
    public boolean checkPassword(String username,String password)
    {
        if(userService.checkUserName(username)){
            {
                User user = userService.getUserByName(username);
                if(password.equals(user.getPassword()))
                    return true;
                else
                    return false;
            }
        }
        else
        {
            return false;
        }
    }

    @RequestMapping(value = "/to_register")//将数据保存到model中，传给register
    public ModelAndView index()
    {
        ModelAndView modelAndView = new ModelAndView("register_test");
        return modelAndView;
    }

    @GetMapping(value = "/to_forgetPassword")//将数据保存到model中，传给register
    public String forgetPassword()
    {
        return "forgetPassword";
    }

    //注册
    @PostMapping(value = "/register")
    @Transactional
    public ModelAndView register(@RequestParam("username")String username, @RequestParam("password")String password, @RequestParam("email")String email){
        ModelAndView success = new ModelAndView();
        try{
            if(!userService.checkUserName(username))
            {
                //获取并封装数据
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setEmail(email);
                user.setActive_status(0);
                user.setActiveCode(IDUtils.getUUID());
                //创建s3用户
                org.twonote.rgwadmin4j.model.User S3User = s3Admin.createS3User(username);
                user.setAccess_key(S3User.getS3Credentials().get(0).getAccessKey());
                user.setSecret_key(S3User.getS3Credentials().get(0).getSecretKey());

                userService.add(user,true);
            }
            else
            {
                success.setViewName("register_fail");
                System.out.println("用户名重复！");
                return success;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        success.setViewName("register_success");
        return success;
    }

    //激活校验码
    @GetMapping(value = "/register/checkCode")
    public ModelAndView registerActive(@RequestParam("code") String code){
        ModelAndView result= new ModelAndView();
        try{
//          根据激活码查询用户
            User user = userService.getUserByActiveCode(code);
//          如果返回的user的ID值为空 则表示注册用户(激活or未激活)中没有此返回的user
            if(user.getId()!=null){
                user.setActive_status(1);
                user.setActiveCode(null);
                userService.activeUser(user);
                result.setViewName("active_success");
            }
            else {
                result.setViewName("active_fail");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }


//    //修改密码
//    @RequestMapping("/reSetPassword")
//    public ModelAndView reset(){
//        ModelAndView mv = new ModelAndView("reSetPassword");
//        return mv;
//    }
//
//    //重置密码
//    @PostMapping("/reSetPassword")
//    public ModelAndView reset(@RequestParam("email") String email) throws Exception {
//        ModelAndView mv = new ModelAndView();
//
//        Integer securityCode= SecurityCode.GetSecurityCode();
//        // 根据邮箱查询用户
//        User user = userService.findUserByMail(email);
//
//        if(user.getId()!=null){
//            //如果查询到有此用户 设置默认安全码并给邮箱发送验证码
//            user.setSecurityCode(securityCode);
//            userService.updateSecurityCode(user);
//            userService.SendSecurityCode(email,securityCode);
//
//            mv.setViewName("reSetPasswords");
//        }
//        else{
//            //若没有查到则提示错误
//            mv.setViewName("reSetPasswords_fail");
//        }
//
//        return mv;
//    }

    //发送重置密码的激活码
    @PostMapping("/sendSecurityCode")
    @ResponseBody
    public boolean sendSecurityCode(String username,String email) throws Exception {
        Integer securityCode= SecurityCode.GetSecurityCode();
        User user = userService.getUserByName(username);
        System.out.println("-------  "+user.toString());
        //先查询邮箱是否与用户名相对应
        if(user.getEmail().equals(email))
        {
            user.setSecurityCode(securityCode);
            userService.updateSecurityCode(user);
            userService.SendSecurityCode(email,securityCode);
            System.out.println("激活邮件已发送！");
            return true;
        }
        else
        {
            return false;
        }
    }

    @PostMapping("/resetPassword")
    @ResponseBody
    public boolean resetPassword(String username,String SecurityCode, String password) throws Exception {
        /**
         *@Descriptuion TODO 验证激活码并重设密码
         **/

        Integer sc=new Integer(SecurityCode);
        //获得邮箱账号对于的user
        User user = userService.getUserByName(username);
        Integer a=user.getSecurityCode();

        //如果user的securityCode和验证码一致
        if(a.equals(sc)){
            user.setPassword(password);
            userService.updatePassword(user);

            //重置securityCode为0
            user.setSecurityCode(null);
            userService.updateSecurityCode(user);
            return true;
        }
        else {
            return false;
        }

    }
}
