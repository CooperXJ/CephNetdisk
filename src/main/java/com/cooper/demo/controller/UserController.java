package com.cooper.demo.controller;

import com.cooper.demo.Bean.User;
import com.cooper.demo.service.User.UserServiceImpl;
import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

//@RestController

@Controller
public class UserController {

    @Autowired
    UserServiceImpl userService;

    @RequestMapping(value = "dashboard")//将数据保存到model中，传给register
    public ModelAndView to_dashboard()
    {
        ModelAndView modelAndView = new ModelAndView("dashboard");
        return modelAndView;
    }

    @GetMapping("/user/home")
    public String toHomePage(HttpServletRequest request, Model model)
    {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        model.addAttribute("user",user);
        return "home/home";
    }

    @GetMapping("/user/change")
    public String changeUserInfo(HttpServletRequest request, @RequestParam("imgSrc") File header_img, @RequestParam("username")String username, @RequestParam("nickname")String nickName, @RequestParam("password")String password, @RequestParam("email")String email) throws IOException {
        HttpSession session = request.getSession();
        User usertmp = (User) session.getAttribute("user");

        String imgPath = null;
        if(header_img==null)
        {
            imgPath = usertmp.getHeader_img();
        }
        else
        {
            String imgName = header_img.getName();
            imgPath = "/images/header_img/" +imgName;
        }
        //其余不改  只修改应该修改的
        User user = usertmp;

        user.setUsername(username);
        user.setHeader_img(imgPath);
        user.setEmail(email);
        user.setNick_name(nickName);
        user.setPassword(password);

        session.setAttribute("user",user);
        userService.updateUserInfoByself(user);
        return "redirect:/user/home";
    }

}
