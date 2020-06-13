package com.cooper.demo.service.S3;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;
import org.twonote.rgwadmin4j.model.Quota;
import org.twonote.rgwadmin4j.model.S3Credential;
import org.twonote.rgwadmin4j.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Scope//全局有且仅有一个实例
@Service
public class S3Admin {
    private static final String accessKey = "98SRU6JLWGSPCLZ9UVR4";
    private static final String secretKey = "cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe";
    private static final String adminEndpoint = "http://192.168.43.112:1999/admin";
    private static RgwAdmin rgwAdmin;
    static
    {
         rgwAdmin = new RgwAdminBuilder().accessKey(accessKey).secretKey(secretKey).endpoint(adminEndpoint)
                .build();
    }

    public void getAdmin()
    {
        //装入admin
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        session.setAttribute("cephAdmin",rgwAdmin);
    }

    public User createS3User(String userId) {

//        RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey(accessKey).secretKey(secretKey).endpoint(adminEndpoint)
//                .build();
//
////        //装入admin
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        HttpSession session = request.getSession();
//        session.setAttribute("cephAdmin",rgwAdmin);

        User user = null;

        // create a user
        user = rgwAdmin.createUser(userId);

        if (user != null) {
            // get user S3Credential
            for (S3Credential credential : user.getS3Credentials()) {
                System.out.println("userid: " + credential.getUserId() + ",getAccessKey: " + credential.getAccessKey()
                        + ", getSecretKey: " + credential.getSecretKey());
            }

            // set user quota, such as maxObjects and maxSize(KB)
            rgwAdmin.setUserQuota(userId, 1000, 1024 * 1024 * 6);//设置用户可以使用的空间大小

            Optional<Quota> quota = rgwAdmin.getUserQuota(userId);

            if (quota.isPresent()) {
                System.out.println("quota KB: " + quota.get().getMaxSizeKb());
            }
        } else {
            System.out.println("create user failed");
        }

        return user;
    }
}
