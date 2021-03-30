package com.cooper.demo.Job;

import com.cooper.demo.Bean.User;
import com.cooper.demo.service.File.FileService;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.SimpleDateFormat;
import java.util.List;

public class QuartzJob1 extends QuartzJobBean {
    @Autowired
    FileService fileService;
    @Autowired
    UserServiceImpl userService;
    @Autowired
    S3ServiceImpl s3Service;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<User> userList = userService.getAllUser();

//        for(User user : userList)
//        {
//            System.out.println(user.toString());
//            List<RecoverFile> list = fileService.getDeleteFileList(user.getUsername());
//            AmazonS3 s3 = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
//            for(RecoverFile file:list)
//            {
//                try {
//                    Date now = new Date();
//                    Date deleteTime = sdf.parse(file.getCtime());
//                    int days = (int) ((now.getTime() - deleteTime.getTime())/ (1000 * 60 * 60 * 24));
//                    if(days>14)
//                    {
//                        s3Service.deleteFile(s3,"recover",file.getFileName());
//                        fileService.deleteFilesAuto(user.getUsername(),file.getFileName());
//                    }
//                    else
//                    {
//                        System.out.println("时间未到！！");
//                    }
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
