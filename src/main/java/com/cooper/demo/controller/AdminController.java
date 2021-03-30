package com.cooper.demo.controller;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.cooper.demo.Bean.User;
import com.cooper.demo.service.S3.S3Admin;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import javax.servlet.http.HttpSession;
import java.net.SocketTimeoutException;
import java.util.List;

@Controller
@RequestMapping("admin")

public class AdminController {

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private S3Admin s3Admin;

    @Autowired
    S3ServiceImpl s3Service;

    @Value("${ceph.ak}")
    private  String accessKey;
    @Value("${ceph.sk}")
    private  String secretKey;
    @Value("${ceph.endpoint}")
    private  String adminEndpoint;


    //查询所有的用户列表
    @GetMapping("Users_Info")
    public String list_Users_Info(Model model)
    {
        List<User> userList = userService.getAllUser();
        model.addAttribute("Users_Info",userList);
        //thymeleaf默认拼串
        //此处的User是User文件夹名称
        return "User/Users_list";
    }

    @GetMapping("add_User")
    public String to_add_page()
    {
        //来到添加页面
        return "User/add_User";
    }

    @PostMapping("add_User")
    public String addUser(@RequestParam("username")String username, @RequestParam("password")String password, @RequestParam("email")String email,
                          @RequestParam("active_status")Integer active_Status,@RequestParam("StorageSpace")Integer StorageSpace,@RequestParam("MaxStorageNumber")Integer MaxStorageNumber) throws SocketTimeoutException {
        if(!userService.checkUserName(username))
        {
            //获取并封装数据
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setActive_status(active_Status);
            user.setActiveCode(null);
            user.setStorageSpace(StorageSpace);
            user.setMaxStorageNumber(MaxStorageNumber);
            org.twonote.rgwadmin4j.model.User S3User = s3Admin.createS3User(username);
            user.setAccess_key(S3User.getS3Credentials().get(0).getAccessKey());
            user.setSecret_key(S3User.getS3Credentials().get(0).getSecretKey());

            AmazonS3 s3 = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
            Bucket recover = s3Service.createBucket(s3,"recover");

            RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey(accessKey).secretKey(secretKey).endpoint(adminEndpoint+"/admin")
                    .build();

            rgwAdmin.setIndividualBucketQuota(username,recover.getName(),100,1024*1024*1);

            user.setRecoverBucket(recover.getName());

            userService.add(user,false);
        }
        else
        {
            System.out.println("用户名重复！");
        }
        return "redirect:/admin/Users_Info";
    }

    @GetMapping("/{username}")
    public String toEditPage(@PathVariable("username")String username,Model model)
    {
        //此处需要注意
        //因为我这里是将添加和修改共用一个页面，那么对于传入的model要进行判断，否则会出现空指针异常

        User user = userService.getUserByName(username);
        model.addAttribute("user",user);

        return "User/add_User";
    }

    //修改用户信息
    @PutMapping("/add_User")
    public String updateUserInfo(User user)
    {
        userService.updateUserInfo(user);
        System.out.println(user);
        return "redirect:/admin/Users_Info";
    }

    //删除用户
    @DeleteMapping("/{username}")
    public String deleteUser(@PathVariable("username")String username, HttpSession session)
    {
        //User user_target = userService.getUserByName(username);

        //获取session
        s3Admin.getAdmin();
        RgwAdmin rgwAdmin = (RgwAdmin) session.getAttribute("cephAdmin");

        //通过管理员删除ceph上面的用户
        rgwAdmin.removeUser(username);
        userService.deleteUser(username);
        return "redirect:/admin/Users_Info";
    }
}
