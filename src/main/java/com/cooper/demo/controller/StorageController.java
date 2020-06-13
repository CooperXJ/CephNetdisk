package com.cooper.demo.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Owner;
import com.cooper.demo.Bean.User;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class StorageController {

    private AmazonS3 S3client;

    @Autowired
    private S3ServiceImpl s3Service;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/user/Storage")
    public String toStoragePage()
    {
        return "Storage/Storage";
    }

    @PostMapping("/user/Storage")
    @ResponseBody
    public JSONArray getStorageInfo(HttpSession httpSession)
    {
        //获得User
        User user = (User)httpSession.getAttribute("user");

        S3client = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
        List<Bucket> bucketList = S3client.listBuckets();

        RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey("98SRU6JLWGSPCLZ9UVR4").secretKey("cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe").endpoint("http://192.168.43.112:1999/admin")
                .build();

        //获得用户的总存储容量  需要减去1G,因为这1G是恢复误删用的
        long totalSpace = rgwAdmin.getUserQuota(user.getUsername()).get().getMaxSizeKb() - 1024*1024*1;

        //已经使用空间总合
        float usedSpace = 0;

        JSONArray jsonArray = new JSONArray();
        for(Bucket bucket:bucketList)
        {
            JSONObject jsonObject = new JSONObject();
            String bucketName = bucket.getName();
            long space;
            try{
                if(s3Service.getBucketName(bucketName).equals("recover"))
                {
                    //直接跳过recover桶
                    continue;
                }
                else
                {
                    space = rgwAdmin.getBucketInfo(bucketName).get().getUsage().getRgwMain().getSize_kb_actual();
                }
            }catch (Exception e)
            {
                space  = 0;
            }

            jsonObject.put("name",s3Service.getBucketName(bucketName));
            jsonObject.put("value",space/Float.valueOf(totalSpace)*totalSpace/1024);
            usedSpace+=space/Float.valueOf(totalSpace);
            jsonArray.add(jsonObject);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","未使用空间");
        jsonObject.put("value",(1-usedSpace)*totalSpace/1024);
        jsonArray.add(jsonObject);

        return jsonArray;
    }

    @PostMapping(value = "/user/Storage/setStorageSpace")
    public String setStorageSpace(@RequestParam("StorageSpace")Integer storageSpace,HttpSession session)
    {
        User user = (User)session.getAttribute("user");
        RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey("98SRU6JLWGSPCLZ9UVR4").secretKey("cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe").endpoint("http://192.168.43.112:1999/admin")
                .build();
        long changedSpace = storageSpace*(1024*1024)+rgwAdmin.getUserQuota(user.getUsername()).get().getMaxSizeKb();

        rgwAdmin.setUserQuota(user.getUsername(),user.getMaxStorageNumber(),changedSpace);

        //更新数据库
        userService.setStorageSpace(user, (int) changedSpace);

        return "redirect:/user/Storage";
    }
}
