package com.cooper.demo.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.cooper.demo.Bean.MyBucket;
import com.cooper.demo.Bean.RecoverFile;
import com.cooper.demo.Bean.UploadObject;
import com.cooper.demo.Bean.User;
import com.cooper.demo.service.File.FileService;
import com.cooper.demo.service.S3.DownloadPart;
import com.cooper.demo.service.S3.S3ServiceImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "user/file")
public class FileController {

    @Autowired
    private   S3ServiceImpl s3Service;
    @Autowired
    HttpServletRequest request;

    @Autowired
    FileService fileService;

    private DownloadPart downloadPart;

    private AmazonS3 S3client;

    private List<Bucket> bucketList;

    private List<MyBucket> buckets;

    private User user;
    //保证session是同一个
    private HttpSession session;

    //保证是同一个list
    private List<S3ObjectSummary> fileList;
    //查询所有的用户所有文件
    @GetMapping("/file_operation")
    public String Get_list_File_Info(ModelMap model)
    {
        session = request.getSession();
        user = (User)session.getAttribute("user");

        S3client = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
        //将创建的s3客户端存放入session中
        session.setAttribute("S3Client",S3client);

        bucketList = s3Service.getBuckets(S3client);

        buckets = new ArrayList<>();
        for(Bucket bucket : bucketList)
        {
            //将recover过滤掉，不展示给用户
            if(s3Service.getBucketName(bucket).equals("recover"))
            {
                session.setAttribute("recoverBucket",bucket.getName());
            }
            else
            {
                MyBucket myBucket = new MyBucket(bucket,s3Service);
                buckets.add(myBucket);
            }
        }

        model.addAttribute("Bucket_Info",buckets);
        return "File/Folder_list";
    }

    @GetMapping("/watch/search/result")
    public String Search(ModelMap model)
    {
        model.addAttribute("File_Info",fileList);
        return "File/Search";
    }

    @PostMapping("/watch/search")
    @ResponseBody
    public void Search(String bucketName,ModelMap model,String prefix)
    {
        bucketName = s3Service.getBucketName(bucketName);
        fileList = s3Service.getFilesFromBucket(S3client,bucketName,prefix);
        //System.out.println(fileList);
    }

//    //查询所有的用户所有文件  因为是接收request传回的post请求，所以需要重新写一个PostMapping
//    @PostMapping("/file_operation")
//    public String Post_list_File_Info(Model model)
//    {
//        //buckets已经有两个了所以不用再重新生成了
//        model.addAttribute("Bucket_Info",buckets);
//        return "File/File_list";
//    }

    //删除桶
    @DeleteMapping("/delete/{bucketName}")
    public String deleteBuckets(@PathVariable("bucketName")String bucketName) throws SocketTimeoutException {
        bucketName = s3Service.getBucketName(bucketName);
        s3Service.deleteBucket(S3client,bucketName);
        return "redirect:/user/file/file_operation";
    }

    //查看指定文件夹下的文件
    @GetMapping("/watch/{bucketName}")
    public String watchFiles(@PathVariable("bucketName")String bucketName,Model model) throws SocketTimeoutException {
        //将桶名称传入到session中，方便调用
        session.setAttribute("bucketName",bucketName);


        bucketName = s3Service.getBucketName(bucketName);

        //System.out.println(bucketName);
        fileList = s3Service.getFilesFromBucket(S3client,bucketName);
        model.addAttribute("File_Info",fileList);
        return "File/FileOfFolder_list";
    }

    //查看指定文件夹下的文件  post请求
    @PostMapping("/watch/{bucketName}")
    public String Get_watchFiles(@PathVariable("bucketName")String bucketName,ModelMap model) throws SocketTimeoutException {

        bucketName = s3Service.getBucketName(bucketName);

        fileList = s3Service.getFilesFromBucket(S3client,bucketName);
        model.addAttribute("File_Info",fileList);
        return "File/FileOfFolder_list";
    }

    //删除文件
    @DeleteMapping("/delete/{bucketName}/{fileName}")
    public String deleteFiles(@PathVariable("bucketName")String bucketName,@PathVariable("fileName")String fileName) throws SocketTimeoutException {

        session = request.getSession();
        String recover = (String) session.getAttribute("recoverBucket");

//        System.out.printf("bukcetName  "+bucketName+"  recoverName:   "+recover);

        //将删除的小文件迁移到recover中，大文件直接删除
        S3client.copyObject(bucketName,fileName,recover,fileName);

        //将删除文件的信息插入到对于的Sql中
        RecoverFile file = new RecoverFile();
        file.setUsername(user.getUsername());
        file.setFileName(fileName);
        file.setBucketName(bucketName);

        //获取当前系统时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        file.setCtime(df.format(new Date()));

        fileService.InsertSqlHistory(file);

        //软删除
        bucketName = s3Service.getBucketName(bucketName);
        s3Service.deleteFile(S3client,bucketName,fileName);

        return "redirect:/user/file/watch/{bucketName}";
    }

    //下载文件
    @GetMapping({"/download/{bucketName}/{fileName}"})
    public String downloadFile(@PathVariable("bucketName")String bucketName,@PathVariable("fileName")String fileName)
    {
        //这里的前缀路径需要自己设置好，因为用户隐私安全所以你不可以去访问用户的目录
        String out_path = "/Users/xuejin/Desktop/S3/download/";
        downloadPart = new DownloadPart(user.getAccess_key(),user.getSecret_key());
        int fileSize = downloadPart.getSize(user.getAccess_key(),user.getSecret_key(),bucketName,fileName);
        if(fileSize<1*1024*1024)
        {
            out_path = out_path+fileName;
            bucketName = s3Service.getBucketName(bucketName);
//            //获得文件后缀名
//            String fileExname = fileName.substring(fileName.lastIndexOf(".")+1);
//            out_path = out_path+newName+"."+fileExname;

            s3Service.downloadFromS3(S3client,bucketName,fileName,out_path);
        }
        else {
            downloadPart.download(user.getAccess_key(),user.getSecret_key(),bucketName,fileName,out_path);
        }
        return "redirect:/user/file/watch/{bucketName}";
    }


//    @RequestMapping(value = "/file_operation")//将数据保存到model中，传给register
//    public ModelAndView to_file_operation(){
//        ModelAndView modelAndView = new ModelAndView("file_operation");
//        return modelAndView;
//    }

    @GetMapping("/to_uploadFile_page")
    public String toUploadPage()
    {
        return "File/upload_file";
    }

    @GetMapping("/to_createNewFolder_page")
    public String toCreateNewFolderPage()
    {
        return "File/CreateNewFolder";
    }

    //创建新的文件夹
    @PostMapping("/createNewFolder")
    @ResponseBody
    public boolean CreateNewFolderPage(String folderName) throws SocketTimeoutException {
        if(folderName!=null&&!folderName.equals(""))
        {
            //从session中获取用户对象
            HttpSession session = request.getSession();
            User user = (User)session.getAttribute("user");

            S3client = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
            if(s3Service.isBucketExists(S3client,folderName))
            {
                return false;
            }
            else {
                s3Service.createBucket(S3client,folderName);
                return true;
            }

        }
       else {
           return false;
        }

    }

    //显示上传过程
    @PostMapping("/uploadInfo")
    @ResponseBody
    public JSONObject uploadResult(){
        HttpSession session = request.getSession();
        //这是第几段
        Object res = session.getAttribute("upload_info");
        //总共的段数
        Object length = session.getAttribute("file_length");
        //名称数组
        Object fileNameList = session.getAttribute("fileNameList");

        if(res==null||length==null||fileNameList==null)
        {
            return null;
        }
        else{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file_length", ((int) length));
            jsonObject.put("upload_info",((int)res));
            return jsonObject;
        }
    }

    //下载文件
    @PostMapping("/download")
    @ResponseBody
    public String download(String bucketName,String fileName)
    {
        //这里的前缀路径需要自己设置好，因为用户隐私安全所以你不可以去访问用户的目录
        String out_path = "/Users/xuejin/Desktop/S3/download/";
        downloadPart = new DownloadPart(user.getAccess_key(),user.getSecret_key());
        int fileSize = downloadPart.getSize(user.getAccess_key(),user.getSecret_key(),bucketName,fileName);
        if(fileSize<1*1024*1024)
        {
            out_path = out_path+fileName;
            bucketName = s3Service.getBucketName(bucketName);
//            //获得文件后缀名
//            String fileExname = fileName.substring(fileName.lastIndexOf(".")+1);
//            out_path = out_path+newName+"."+fileExname;

            s3Service.downloadFromS3(S3client,bucketName,fileName,out_path);
            return "ok";
        }
        else {
            downloadPart.download(user.getAccess_key(),user.getSecret_key(),bucketName,fileName,out_path);
            return "ok";
        }
    }

    //上传队列显示Get
    @GetMapping("/upload")
    @ResponseBody
    public JSONArray getUploadList()
    {
        HttpSession session = request.getSession();
        Object object = session.getAttribute("uploadList");
        if(object==null)
        {
            return null;
        }
        else {
            //List<String> uploadList = (List<String>) object;

            List<UploadObject> uploadList = (List<UploadObject>) object;

            JSONArray jsonArray = JSONArray.fromObject(uploadList);
            return jsonArray;
        }
    }

    @PostMapping("/preview")
    @ResponseBody
    public String toPreviewPage(String bucketName,String fileName)
    {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 60 * 24 * 7;
        expiration.setTime(expTimeMillis);
        //获取一个request
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                bucketName,fileName).withExpiration(expiration);
        //生成公用的url
        URL url = S3client.generatePresignedUrl(urlRequest);
        return url.toString();
    }

    @PostMapping("/share")
    @ResponseBody
    public String toCopyUrlPage(String bucketName,String fileName)
    {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 60 * 24 * 7;//设置过期时间  一周
        expiration.setTime(expTimeMillis);
        //获取一个request
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                bucketName,fileName).withExpiration(expiration);
        //生成公用的url
        URL url = S3client.generatePresignedUrl(urlRequest);
        return url.toString();
    }

    @GetMapping("/recover")
    public String toRecoverPage()
    {
        return "Recover/Recover";
    }

    @PostMapping("/getDeletedFileList")
    @ResponseBody
    public JSONArray getDeletedFileList(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        System.out.println("username  "+user.getUsername());
        JSONArray jsonArray = new JSONArray();

        List<RecoverFile> fileList = fileService.getDeleteFileList(user.getUsername());
        for(RecoverFile file:fileList)
        {
            jsonArray.add(file.toJson());
        }
        return jsonArray;
    }

    @PostMapping("/getDeletedFileList/recover")
    @ResponseBody
    public void recoverFile(String bucketName, String fileName,HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String recover = (String) session.getAttribute("recoverBucket");
        User user = (User) session.getAttribute("user");

        S3client.copyObject(recover,fileName,bucketName,fileName);

        s3Service.deleteFile(S3client,s3Service.getBucketName(recover),fileName);

        fileService.deleteSqlHistory(user.getUsername(),fileName,bucketName);
    }

    @PostMapping("/getDeletedFileList/delete")
    @ResponseBody
    public void deleteFile(String bucketName,String fileName,HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        String recover = (String) session.getAttribute("recoverBucket");
        User user = (User) session.getAttribute("user");
        System.out.println(fileName);
        s3Service.deleteFile(S3client,s3Service.getBucketName(recover),fileName);

        fileService.deleteSqlHistory(user.getUsername(),fileName,bucketName);
    }

//    @GetMapping("/download/stop")
//    public String downloadStop()
//    {
//        session= request.getSession();
//        session.setAttribute("flag",false);
//        System.out.println("**********test");
//        return "redirect:/user/file/file_operation";
//    }

    //文件操作
//    @PostMapping(value = "/upload")//将数据保存到model中，传给register
//    public String file_operation(@RequestParam("bucketName")String bucketName, @RequestParam("upload_path")String upload_path, @RequestParam("file_name")String file_name, @RequestParam("file") MultipartFile[] files) throws IOException {
//
//        if(upload_path!=null)
//        {
//            //从session中获取用户对象
//            HttpSession session = request.getSession();
//            User user = (User)session.getAttribute("user");
//            System.out.println(user.toString());
//
//            S3client = s3Service.getS3Client(user.getAccess_key(),user.getSecret_key());
//
//            System.out.println("upload_path:  "+upload_path);
//
//            //s3Service.uploadToS3(S3client,bucketName,new File(file_name),file_name);
//            //System.out.println("file   "+file.getName()+"   path: " +file.getAbsolutePath());
//            int len = files.length;
//            if(len>0)
//            {
//                for(MultipartFile file:files)
//                {
//                    if(!file.isEmpty())
//                    {
//                        String fileName = file.getOriginalFilename();
//                        System.out.println("FileName   "+file.toString());
//                        //s3Service.uploadToS3(S3client,bucketName,(File)file,fileName);
//                    }
//                }
//            }
//            //s3Service.uploadToS3(S3client,bucketName,new File(file.getAbsolutePath()),file.getName());
//            return "redirect:/user/file/file_operation";
//        }
//        else {
//            return "redirect:/user/file/file_operation";
//        }
//    }

}
