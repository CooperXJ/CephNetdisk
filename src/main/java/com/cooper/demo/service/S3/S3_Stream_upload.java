package com.cooper.demo.service.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.cooper.demo.Bean.UploadBigObject;
import com.cooper.demo.Bean.UploadObject;
import com.cooper.demo.Bean.UploadSmallObject;
import com.cooper.demo.common.FileContentTypeUtils;
import com.cooper.demo.common.IDUtils;
import net.sf.json.JSONArray;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class S3_Stream_upload extends HttpServlet {

    private  AmazonS3 s3;
    //分段传输的请求的最小段大小
    private byte[] bytes = new byte[1024*1024*5];
    private final long slice_size = 1024*1024*5;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //从session中获取S3Client和bucketName
        HttpSession session = req.getSession();
        s3 = (AmazonS3) session.getAttribute("S3Client");

        String bucketName = (String) session.getAttribute("bucketName");

        //得到上传文件的保存目录，将上传的文件存放在WEB-INF目录下面 不允许外界直接访问，保证上传文件的安全
        //String savePath = this.getServletContext().getRealPath("/LoadJsp");
        //String tempPath = this.getServletContext().getRealPath("/LoadJsp/temp");


        String tempPath = "/Users/xuejin/Desktop/S3/upload";
        File file  =new File(tempPath);
        if(!file.exists()&&!file.isDirectory()){
            System.out.println(tempPath+"目标目录不存在，需要进行创建");
            file.mkdir();
        }

        String message = null;
        try {
            //1 创建DiskFileItemFactory工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //设置缓冲区得大小
            factory.setSizeThreshold(1024*1000000);
            //设置上传时生成得临时文件保存目录
            factory.setRepository(file);
            //2 创建一个文件上传解析器  指定使用缓存区与临时文件存储位置.
            ServletFileUpload upload = new ServletFileUpload(factory);

            //session.setAttribute("upload_info",0);

            //监听文件上传速度
            upload.setProgressListener(new ProgressListener() {
                @Override
                public void update(long pBytesRead, long pContentLength, int arg2) {
                    //System.out.println("文件大小为:" + pContentLength+"当前处理"+pBytesRead);
                }
            });
            //判断提交上来的数据是不是表单上的数据
            //解决乱码名称
            upload.setHeaderEncoding("UTF-8");
            if (!ServletFileUpload.isMultipartContent(req)) {
                System.out.println("failure----------");
                return;
            }
            //设置上传文件总量得最大值
            upload.setSizeMax(1024*10*1024000);
            //设置单个上传文件得最大值
            upload.setFileSizeMax(1024*1000000);

            //删除掉非文件的提交项
            List<FileItem> list = upload.parseRequest(req);
            //List<String> uploadList = new ArrayList<>();
            List<UploadObject> uploadList = new ArrayList<>();

            int order = 0;
            for(FileItem fileItem:list)
            {
                if(fileItem.isFormField())
                {
                    fileItem.delete();
                }
                else {
                    UploadObject uploadObject = new UploadObject();
                    uploadObject.setOrder(order);
                    uploadObject.setFileName(fileItem.getName());
                    uploadObject.setIs_done(0);
                    uploadList.add(uploadObject);
                    order++;
                }
            }
            //事前先将即将上传的文件民成装载到session中
            session.setAttribute("uploadList",uploadList);

            List<FileItem> smallFileList  = new ArrayList<>();
            List<FileItem> bigFileList = new ArrayList<>();
            for(FileItem fileItem:list)
            {
                //如果上传相同的文件名的文件则会被覆盖掉
                //如果文件大于100M则使用分段传输
                if(fileItem.getSize()/1024.0/1024.0>20)
                {
                    bigFileList.add(fileItem);
                }
                else
                {
                    smallFileList.add(fileItem);
                }
            }

            //小文件上传线程
            Runnable r1 = ()->{
                int len;
                if(smallFileList.size()%2==0)
                {
                    len = smallFileList.size();
                }
                else
                {
                    len = smallFileList.size()+1;
                }

              for(int i =0;i<len/2;i++)
              {
                  ObjectMetadata objectMetadata = new ObjectMetadata();
                  //获得后缀名

                  String fileName = list.get(i).getName();
                  String fileExname = fileName.substring(fileName.lastIndexOf("."));

                  System.out.println("thread-1  "+fileName);

                  //设置Content-type
                  objectMetadata.setContentType(FileContentTypeUtils.contentType(fileExname));

                  UploadSmallObject uploadSmallObject = new UploadSmallObject();
                  uploadSmallObject.setFileName(fileName);
                  uploadSmallObject.setFileUploadProgress("notStart");

                  FileItem fileItem = smallFileList.get(i);

                  String name = fileItem.getName();
                  if(name==null||name.trim().equals(""))
                  {
                      continue;
                  }
                  else
                  {
                      S3ServiceImpl s3Service = new S3ServiceImpl();
                      try
                      {
                          s3Service.uploadToS3(s3,bucketName,fileItem,fileName,objectMetadata);

                          //通知已经完成上传
                          for(UploadObject uploadObject:uploadList)
                          {
                              if(uploadObject.getFileName().equals(uploadSmallObject.getFileName()))
                              {
                                  uploadObject.setIs_done(1);
                              }
                          }
                          session.setAttribute("uploadList",uploadList);

                          uploadSmallObject.setFileUploadProgress("ok");
                          session.setAttribute("Thread_1_upload",uploadSmallObject);
                      }catch (Exception e)
                      {
                          e.printStackTrace();
                          uploadSmallObject.setFileUploadProgress("failure");
                          session.setAttribute("Thread_1_upload",uploadSmallObject);
                      }
                  }
                  }
            };

            //小文件上传线程
            Runnable r2 = ()->{
                int len;
                if(smallFileList.size()%2==0)
                {
                    len = smallFileList.size();
                }
                else
                {
                    len = smallFileList.size()+1;
                }

                for(int j =len/2;j<smallFileList.size();j++)
                {
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    //获得后缀名
                    String fileName = list.get(j).getName();
                    String fileExname = fileName.substring(fileName.lastIndexOf("."));

                    System.out.println("thread-2  "+fileName);
                    //设置Content-type
                    objectMetadata.setContentType(FileContentTypeUtils.contentType(fileExname));

                    UploadSmallObject uploadSmallObject = new UploadSmallObject();
                    uploadSmallObject.setFileName(fileName);
                    uploadSmallObject.setFileUploadProgress("notStart");

                    FileItem fileItem = smallFileList.get(j);

                    String name = fileItem.getName();
                    if(name==null||name.trim().equals(""))
                    {
                        continue;
                    }
                    else
                    {
                        S3ServiceImpl s3Service = new S3ServiceImpl();
                        try
                        {
                            s3Service.uploadToS3(s3,bucketName,fileItem,fileName,objectMetadata);

                            //通知已经完成上传
                            for(UploadObject uploadObject:uploadList)
                            {
                                if(uploadObject.getFileName().equals(uploadSmallObject.getFileName()))
                                {
                                    uploadObject.setIs_done(1);
                                }
                            }
                            session.setAttribute("uploadList",uploadList);

                            uploadSmallObject.setFileUploadProgress("ok");
                            session.setAttribute("Thread_2_upload",uploadSmallObject);
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                            uploadSmallObject.setFileUploadProgress("failure");
                            session.setAttribute("Thread_2_upload",uploadSmallObject);
                        }
                    }
                }
            };

            //大文件上传线程
            Runnable r3 = ()->{
                int len;
                if(bigFileList.size()%2==0)
                {
                    len = bigFileList.size();
                }
                else
                {
                    len = bigFileList.size()+1;
                }

                for(int k =0;k<len/2;k++)
                {
                    FileItem fileItem = bigFileList.get(k);
                    long size = fileItem.getSize();
                    UploadBigObject uploadObject = new UploadBigObject();

                    uploadObject.setFileSize(size);

                    String name = fileItem.getName();
                    System.out.println("thread-3  "+name);

                    if(name==null||name.trim().equals(""))
                    {
                        continue;
                    }
                    else
                    {
                        try {
                            uploadObject.setFileName(name);

                            InputStream inputStream = fileItem.getInputStream();
                            String key = name;
                            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, key);
                            InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);

                            int bytesRead = 0;
                            int partNumber = 1;
                            long total_bytes = 0;
                            List<PartETag> results = new ArrayList<>();
                            while ((bytesRead=inputStream.read(bytes))>= 0) {
                                total_bytes+=bytesRead;
                                //bytesRead = inputStream.read(bytes);//从内存中读取一定的数据并存入到bytes数组中并返回长度
                                UploadPartRequest part = new UploadPartRequest()
                                        .withBucketName(bucketName)
                                        .withKey(key)
                                        .withUploadId(initResponse.getUploadId())
                                        .withPartNumber(partNumber)
                                        .withInputStream(new ByteArrayInputStream(bytes, 0, bytesRead))
                                        .withPartSize(bytesRead);
                                results.add(s3.uploadPart(part).getPartETag());
                                partNumber++;
                                uploadObject.setFileUploadProgress(total_bytes);
                                //将uploadObject装入到session中
                                session.setAttribute("Thread_3_upload",uploadObject);
                                //System.out.println("session: "+uploadObject.toString());
                            }
                            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest()
                                    .withBucketName(bucketName)
                                    .withKey(key)
                                    .withUploadId(initResponse.getUploadId())
                                    .withPartETags(results);
                            s3.completeMultipartUpload(completeRequest);

                            //发送成功的信息
                            //通知已经完成上传
                            for(UploadObject object:uploadList)
                            {
                                if(object.getFileName().equals(uploadObject.getFileName()))
                                {
                                    object.setIs_done(1);
                                }
                            }
                            session.setAttribute("uploadList",uploadList);

                            uploadObject.setFileUploadProgress(size);
                            session.setAttribute("Thread_3_upload",uploadObject);
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Runnable r4 = ()->{
                int len;
                if(bigFileList.size()%2==0)
                {
                    len = bigFileList.size();
                }
                else
                {
                    len = bigFileList.size()+1;
                }

                for(int l =len/2;l<bigFileList.size();l++)
                {

                    FileItem fileItem = bigFileList.get(l);
                    long size = fileItem.getSize();
                    UploadBigObject uploadObject = new UploadBigObject();

                    uploadObject.setFileSize(size);

                    String name = fileItem.getName();
                    System.out.println("thread-4  "+name);
                    if(name==null||name.trim().equals(""))
                    {
                        continue;
                    }
                    else
                    {
                        try {
                            uploadObject.setFileName(name);

                            InputStream inputStream = fileItem.getInputStream();
                            String key = name;
                            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, key);
                            InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);

                            int bytesRead = 0;
                            int partNumber = 1;
                            long total_bytes = 0;
                            List<PartETag> results = new ArrayList<>();
                            while ((bytesRead=inputStream.read(bytes))>= 0) {
                                total_bytes+=bytesRead;
                                //bytesRead = inputStream.read(bytes);//从内存中读取一定的数据并存入到bytes数组中并返回长度
                                UploadPartRequest part = new UploadPartRequest()
                                        .withBucketName(bucketName)
                                        .withKey(key)
                                        .withUploadId(initResponse.getUploadId())
                                        .withPartNumber(partNumber)
                                        .withInputStream(new ByteArrayInputStream(bytes, 0, bytesRead))
                                        .withPartSize(bytesRead);
                                results.add(s3.uploadPart(part).getPartETag());
                                partNumber++;
                                uploadObject.setFileUploadProgress(total_bytes);
                                //将uploadObject装入到session中
                                session.setAttribute("Thread_4_upload",uploadObject);
                                //System.out.println("session: "+uploadObject.toString());
                            }
                            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest()
                                    .withBucketName(bucketName)
                                    .withKey(key)
                                    .withUploadId(initResponse.getUploadId())
                                    .withPartETags(results);
                            s3.completeMultipartUpload(completeRequest);

                            //发送成功的信息
                            for(UploadObject object:uploadList)
                            {
                                if(object.getFileName().equals(uploadObject.getFileName()))
                                {
                                    object.setIs_done(1);
                                }
                            }
                            session.setAttribute("uploadList",uploadList);

                            uploadObject.setFileUploadProgress(size);
                            session.setAttribute("Thread_4_upload",uploadObject);
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            Thread t3 = new Thread(r3);
            Thread t4 = new Thread(r4);
            t1.start();
            t2.start();
            t3.start();
            t4.start();

            try{
                t1.join();
                t2.join();
                t3.join();
                t4.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("上传成功！");
            //4 使用ServletFileUpload解析器来解析上传数据，解析结果返回的是一个List<FileItem>
            //集合，每一个FileItem对应一个Form表单的输入项
            //List<FileItem> list = upload.parseRequest(req);
//            for(FileItem item:list) {
//                if (item.isFormField()) {
//                    String name = item.getFieldName();
//                    String value = item.getString("UTF-8");
//                    System.out.println(name + "=" + value);
//                } else {
//                    String filename = item.getName();
//                    //获得文件大小
//                    long size = item.getSize();
//
//                    System.out.println(filename);
//                    if (filename == null || filename.trim().equals("")) {
//                        continue;
//                    }
//                    //注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：  c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
//                    //处理获取到的上传文件的文件名的路径部分，只保留文件名部分
//                    filename = filename.substring(filename.lastIndexOf("\\") + 1);
//                    //获得文件扩展名字
//                    //String  fileExname = filename.substring(filename.lastIndexOf(".")+1);
//                    //System.out.println("上传的文件的扩展名是："+fileExname);
//                    //获取item输入流
//                    InputStream inputStream = item.getInputStream();
////                    //得到保存文件得名称
////                    String saveFilename  = makeFileName(filename);
////                    //得到文件得保存目录
////                    String realSavaPath = makePath(saveFilename,savePath);
////                    //创建一个文件输出流
////
////                    FileOutputStream fileOutputStream  =new FileOutputStream(realSavaPath
////                            +"\\"+saveFilename);
////                    //创建一个缓冲区
////                    byte buffer[] = new byte[1024];
////                    //判断输入流是否已经读完的标识
////                    int len = 0;
//
////                    byte[] contentBytes = IOUtils.toByteArray(inputStream);
////                    Long contentLength = Long.valueOf(contentBytes.length);
////                    ObjectMetadata metadata = new ObjectMetadata();
////                    metadata.setContentLength(contentLength);
////
////                    //上传
////                    PutObjectRequest putObjectRequest = new PutObjectRequest("test-597eeea97aed46409820396cb2239de5","key",inputStream,metadata);
////                    s3.putObject(putObjectRequest);
////                    while ((len=inputStream.read(buffer))>0)
////                    {
////                        //fileOutputStream.write(buffer,0,len);
////                    }
//                    //inputStream.close();
//
//                    //共分成几段
//                    int length;
//                    if(size%(1024*1024*5)!=0)
//                    {
//                        length = (int)size/(1024*1024*5)+1;
//                    }
//                    else
//                    {
//                        length = (int)size/(1024*1024*5);
//                    }
//
//                    //添加到session中
//                    session.setAttribute("file_length",length);
//
//                    //String uploadId = s3.initiateMultipartUpload(new InitiateMultipartUploadRequest("test-597eeea97aed46409820396cb2239de5", "key")).getUploadId();
//
//                    //System.out.println("文件名为  "+item.getName());
//                    String key = item.getName();
//                    InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, key);//test-597eeea97aed46409820396cb2239de5
//                    InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);
//
//                    int bytesRead = 0;
//                    int partNumber = 1;
//                    List<PartETag> results = new ArrayList<>();
//                    while ((bytesRead=inputStream.read(bytes))>= 0) {
//                        //System.out.println("这是一次上传");
//                        //bytesRead = inputStream.read(bytes);//从内存中读取一定的数据并存入到bytes数组中并返回长度
//                        //System.out.println(bytesRead);
//                        UploadPartRequest part = new UploadPartRequest()
//                                .withBucketName(bucketName)
//                                .withKey(key)
//                                .withUploadId(initResponse.getUploadId())
//                                .withPartNumber(partNumber)
//                                .withInputStream(new ByteArrayInputStream(bytes, 0, bytesRead))
//                                .withPartSize(bytesRead);
//                        results.add(s3.uploadPart(part).getPartETag());
//                        partNumber++;
//                        session.setAttribute("upload_info",partNumber);
//                    }
//                    CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest()
//                            .withBucketName(bucketName)
//                            .withKey(key)
//                            .withUploadId(initResponse.getUploadId())
//                            .withPartETags(results);
//                    s3.completeMultipartUpload(completeRequest);
//
//                    inputStream.close();
//                    item.delete();
//                    message = "file upload done!";
//                    System.out.println(message);
//                    session.setAttribute("file_length",0);
//                    session.setAttribute("upload_info",0);
//                }
//            }
        }catch (Exception e)
        {
            message = "文件上传失败";
            e.printStackTrace();
        }
        req.setAttribute("message",message);
        //System.out.println("当前路径为   "+req.getContextPath());
        String url = "/user/file/watch/"+bucketName;
        req.getRequestDispatcher(url).forward(req,resp);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doGet(req, resp);
        resp.setContentType("application/json; charset=utf-8");
        resp.setHeader("cache-control", "no-cache");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        Object object = session.getAttribute("uploadList");
        if(object==null)
        {

        }
        else {
            List<String> uploadList = (List<String>) object;
            JSONArray jsonArray = JSONArray.fromObject(uploadList);
            out.print(jsonArray);
            out.flush();
            out.close();
        }
    }
}
