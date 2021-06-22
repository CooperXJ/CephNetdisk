package com.cooper.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PresignedUrlDownloadConfig;
import com.amazonaws.services.s3.model.PresignedUrlDownloadRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cooper.demo.Bean.User;
import com.cooper.demo.Mapper.UserMapper;
import com.cooper.demo.common.FileContentTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/6/14 10:34 下午
 */
@Controller
@RequestMapping("/s3")
@Slf4j
public class S3Controller {
    //保证session是同一个
    @Autowired
    private HttpSession session;

    @Value("${ceph.endpoint}")
    private String endpoint;
    @Autowired
    private UserMapper userMapper;

    @Value("${ceph.ak}")
    private String ak;

    @Value("${ceph.sk}")
    private String sk;

    @Value("${ceph.picBucket}")
    private String picBucket;

    private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2+1<4?4:Runtime.getRuntime().availableProcessors()*2+1);

    private Map<String,Double> uploadListenerMap = new ConcurrentHashMap<>();
    private Map<String,Double> downloadListenerMap = new ConcurrentHashMap<>();

    @RequestMapping(value = "upload",method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile[] multipartFiles) throws InterruptedException, IOException {
        User user = (User)session.getAttribute("user");
        AmazonS3 s3 = getS3(user.getAccess_key(), user.getSecret_key());
        String bucket = (String) session.getAttribute("bucketName");
        TransferManager manager = TransferManagerBuilder.standard().withS3Client(s3).build();
        for (MultipartFile multipartFile : multipartFiles) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    String fileName = multipartFile.getOriginalFilename();
                    String fileExname = fileName.substring(fileName.lastIndexOf("."));
                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(multipartFile.getSize());
                    String type = FileContentTypeUtils.contentType(fileExname);
                    if(type!=null){
                        metadata.setContentType(type);
                    }

                    PutObjectRequest request = null;
                    try {
                        request = new PutObjectRequest(bucket, multipartFile.getOriginalFilename(), multipartFile.getInputStream(), metadata);
                    } catch (IOException e) {
                        log.error("无法上传");
                    }
                    Upload upload = manager.upload(request);

                    do {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            return;
                        }
                        TransferProgress progress = upload.getProgress();
                        double pct = progress.getPercentTransferred();
                        uploadListenerMap.put(multipartFile.getOriginalFilename(),pct);
                        System.out.println(multipartFile.getOriginalFilename()+"上传进度为："+pct);
                    } while (upload.isDone() == false);

                    try {
                        UploadResult uploadResult = upload.waitForUploadResult();
                        if(uploadResult!=null){
                            log.info("上传完成:{}",multipartFile.getOriginalFilename());
                        }
                    } catch (Exception e) {
                        upload.abort();
                    }
                }
            });
        }
        return "redirect:/user/file/watch/"+bucket;
    }

    @RequestMapping(value = "getUploadInfo",method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getUploadInfo() {
        JSONArray array = new JSONArray();
        uploadListenerMap.forEach((k,v)->{
            if(v!=100){
                JSONObject object = new JSONObject();
                object.put("fileName",k);
                object.put("fileUploadProgress",v);
                object.put("fileSize",100);
                array.add(object);
            }
        });
        return array;
    }

    @RequestMapping(value = "/download",method = RequestMethod.POST)
    @ResponseBody
    public void download(@RequestParam("files") List<String> files,@RequestParam("downloadPath")String downloadPath) throws InterruptedException {
        User user = (User)session.getAttribute("user");
        AmazonS3 s3 = getS3(user.getAccess_key(), user.getSecret_key());
        String bucket = (String) session.getAttribute("bucketName");
        TransferManager manager = TransferManagerBuilder.standard().withS3Client(s3).build();

        files.forEach(file->{
            if(!file.equals("")){
                pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(new Date());
                        calendar.add(Calendar.DAY_OF_WEEK,1);
                        URL url = s3.generatePresignedUrl(bucket,file, calendar.getTime());
                        PresignedUrlDownloadRequest request = new PresignedUrlDownloadRequest(url);
                        PresignedUrlDownloadConfig config = new PresignedUrlDownloadConfig();
                        config.withDownloadSizePerRequest(1024*1024*10);//默认5M为一个chunk，现在改成10M
//                   config.withResumeOnRetry(true);
                        PresignedUrlDownload download = manager.download(request, new File(downloadPath+file), config);

                        do {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                return;
                            }
                            TransferProgress progress = download.getProgress();
                            double pct = progress.getPercentTransferred();
                            downloadListenerMap.put(file,pct);
                            System.out.println(file+"下载进度为："+pct);
                        } while (download.isDone() == false);

                        try {
                            download.waitForCompletion();
                            log.info(file+"下载完成");
                        } catch (InterruptedException e) {
                            log.error("下载失败");
                            try {
                                download.abort();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    @RequestMapping(value = "getDownloadInfo",method = RequestMethod.GET)
    @ResponseBody
    public JSONArray getDownloadInfo() {
        JSONArray array = new JSONArray();
        downloadListenerMap.forEach((k,v)->{
            if(!k.equals("")&&k!=null&&v!=100){
                JSONObject object = new JSONObject();
                object.put("fileName",k);
                object.put("fileDownloadProgress",v);
                object.put("fileSize",100);
                array.add(object);
            }
        });
        return array;
    }

    @RequestMapping(value = "uploadPic",method = RequestMethod.POST)
    @ResponseBody
    public void uploadFile(@RequestParam("img")MultipartFile multipartFile) throws IOException {
        AmazonS3 s3 = getS3(ak, sk);
        String fileName = multipartFile.getOriginalFilename();
        String fileExname = fileName.substring(fileName.lastIndexOf("."));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        String type = FileContentTypeUtils.contentType(fileExname);
        if(type!=null){
            metadata.setContentType(type);
        }

        s3.putObject(picBucket,multipartFile.getOriginalFilename(),multipartFile.getInputStream(),metadata);
        log.info("上传图片成功！");
    }

    private AmazonS3 getS3(String ak,String sk){
        AWSCredentials credentials = new BasicAWSCredentials(ak,sk);
        return   AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,""))
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
