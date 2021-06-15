package com.cooper.demo;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.cooper.demo.Bean.FileRecover;
import com.cooper.demo.service.File.FileService;
import com.cooper.demo.service.S3.S3ServiceImpl;
import com.cooper.demo.service.User.UserServiceImpl;
import net.sf.json.JSONArray;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.twonote.rgwadmin4j.RgwAdmin;
import org.twonote.rgwadmin4j.RgwAdminBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class S3Test {

    String input_path = "/Users/xuejin/Desktop/S3/1.rtf";
    static String out_path = "/Users/xuejin/Desktop/S3/3.dmg";
    static String out_path_1 = "/Users/xuejin/Desktop/S3/4.dmg";

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private FileService fileService;

    public static void main(String[] args){

//        List<Integer> flag = new ArrayList<>();
//        flag.add(1);
//        flag.add(1);
//
//        String lock = "1";
//
//        Runnable r1 = ()->{
//            int i =0;
//           while (true&&i<10)
//            {
//                synchronized (lock)
//                {
//                    try {
//                        if(flag.get(1)==5)
//                        {
//                            lock.wait();
//                            System.out.println("wait"+flag.get(1));
//                        }
//                        else
//                        {
//                            System.out.println(i);
//                            Thread.sleep(1000);
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                i++;
//            }
//        };
//
//
//
//        Thread t1 = new Thread(r1);
//
//        Runnable r2 = ()->{
//            int i =0;
//            while (true&&i!=-1)
//            {
//                if(i==0)
//                {
//
//                    flag.set(1,5);
//                    try {
//                        Thread.sleep(1000);i++;
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                System.out.println("lock2  "+flag.get(1));
//                synchronized (lock)
//                {
//                    flag.set(1,10);
//                    lock.notify();
//                    System.out.println("notify   "+flag.get(1));
//                    if(!t1.isAlive())
//                    {
//                        i=-1;
//                    }
//                }
//            }
//        };
//
//        Thread t2 = new Thread(r2);
//        t1.start();
//        t2.start();

        Runnable r1 = ()->{
            while(true)
            {
                System.out.println("r1");
            }
        };

        Runnable r2 = ()->{
            while (true)
            {
                System.out.println("r2");
            }
        };
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        t1.start();
        t2.start();

        while(true)
        {
            System.out.println("main");
        }


    }

    @Test
    public void test_connection() throws IOException {
        S3ServiceImpl s3Service = new S3ServiceImpl();
        AmazonS3 s3 = s3Service.getS3Client("7METYYEX1K0DDY3E0ODM","EAuk8pFbeA8i3pNnPY2x68FDXmI452RyMdRZUwse");
        //s3Service.uploadToS3(s3,"abc123",new File(input_path),"key");
        //s3Service.downloadFromS3(s3,"test","1.rtf",out_path_1);

        Runnable runnable = ()->{
            s3Service.downloadFromS3(s3,"test","1.rtf",out_path_1);
            //System.out.println("Hello");
        };

//        Runnable runnable1 = ()->{
//            s3Service.downloadFromS3(s3,"test","1.rtf",out_path);
//            //System.out.println("world");
//        };
        Thread t1 = new Thread(runnable);
//        Thread t2 = new Thread(runnable1);

        t1.start();
//        t2.start();
    }

    static class myThread extends Thread
    {
        private int lock = 1;
        @Override
        public void run() {
            for(int i =0;i<10&&lock==1;i++)
            {
                try {
                    if(i==5)
                        lock = 0;

                    System.out.println(i);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public int getLock() {
            return lock;
        }

        public void setLock(int lock) {
            this.lock = lock;
        }
    }

    //junit单元测试不支持多线程测试
    @Test
    public void test_CreateBucket() throws SocketTimeoutException {
        String access_key = "98SRU6JLWGSPCLZ9UVR4";
        String secret_key = "cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe";
        S3ServiceImpl s3Service = new S3ServiceImpl();
        AmazonS3 s3 = s3Service.getS3Client(access_key,secret_key);
        s3Service.createBucket(s3,"recover");
    }

    @Test
    public void test_GetBucketNames()
    {
        S3ServiceImpl s3Service = new S3ServiceImpl();
        AmazonS3 s3 = s3Service.getS3Client("DC9U2V53JGJ9S2NBJ4YD","YyDf4qohMiG7UYSyUkeOynxWpqkT6igWbWUN26y7");

        for(Bucket bucket : s3Service.getBuckets(s3))
        {
            System.out.println(bucket.toString());
        }
    }

    @Test
    public void test_GetFileFromBucket()
    {
        S3ServiceImpl s3Service = new S3ServiceImpl();
//        AmazonS3 s3 = s3Service.getS3Client("EKYMUJ07JYT88M8IIBLT","gcL22sAgXqKdjf18pDROAPLzBaI2TIWxqWHcRjLE");
        AmazonS3 s3 = s3Service.getS3Client("DC9U2V53JGJ9S2NBJ4YD","YyDf4qohMiG7UYSyUkeOynxWpqkT6igWbWUN26y7");

        List<S3ObjectSummary> list = s3Service.getFilesFromBucket(s3,"test");
        for(S3ObjectSummary file:list)
        {
            System.out.println(file.toString());
        }
        //s3.copyObject("hello-2bddbd0ab6564a5bacb7b6f7df40c2ff","test.mp3","test-abdae0343fc1491a972b201b5db0e827","test.mp3");
    }

    @Test
    public void test_deleteFile() throws IOException {
        S3ServiceImpl s3Service = new S3ServiceImpl();
        //s3Service.uploadToS3("Bbb",new File(input_path),"key");
        //s3Service.deleteBucket("abcd1234-87ba9fdf9b5447f6b6d4ca3af224fd4a");
        AmazonS3 s3 = s3Service.getS3Client("7METYYEX1K0DDY3E0ODM","EAuk8pFbeA8i3pNnPY2x68FDXmI452RyMdRZUwse");
        s3Service.deleteBucket(s3,"abc123");

        //s3Service.isFileExits(s3,"abcd1234","key");
        //s3Service.isBucketExists(s3,"abc123");
        //s3Service.deleteFile(s3,"abc123","key");
    }

    @Test
    public void test()
    {
        List<String> list= new ArrayList<>();
        list.add("123");
        System.out.println(list.get(0));
    }

    @Test
    public void test_upload()
    {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024*1000000);
        ServletFileUpload upload = new ServletFileUpload((FileItemFactory) factory);
        upload.setProgressListener(new ProgressListener() {
            @Override
            public void update(long l, long l1, int i) {
                System.out.println("文件大小为："+l+"  当前处理"+l1);
            }
        });

        upload.setHeaderEncoding("UTF-8");
        upload.setSizeMax(1024*10*1024000);
        upload.setFileSizeMax(1024*1000000);
    }


//    @Test
//    public void testS3() throws Exception{
//
//        String mainBuckt = awsS3Component.getMainBuckt();
//        String s3Key = "9c8bd_2019-05-20.tar.gz";
//        AmazonS3 s3Client = awsS3Component.getInstance();
//        long length = awsS3Component.length(mainBuckt, s3Key);
//        long a = length%100L;
//        System.out.println("总长度："+length);
//        //因为是压缩包格式，所以不适用分段读取内容，会乱码
//        for (int i=0;length>i;i=i+1024){
//            GetObjectRequest getObjectRequest = new GetObjectRequest(mainBuckt,s3Key);
//            getObjectRequest.setRange(i, i+1024);
//            S3Object nosObject = s3Client.getObject(getObjectRequest);
//            long contentLength = nosObject.getObjectMetadata().getContentLength();
//            InputStream objectContent = nosObject.getObjectContent();
//            BufferedReader reader = null;
//            //new BufferedReader(new InputStreamReader(objectContent, "GBK"));
//            //解压
//            if (s3Key.endsWith(".lzo_deflate")) {
//                //lzo解压缩
//                LzoAlgorithm algorithm = LzoAlgorithm.LZO1X;
//                LzoDecompressor deCompressor = LzoLibrary.getInstance().newDecompressor(algorithm, null);
//                LzoInputStream stream = new LzoInputStream(objectContent, deCompressor);
//                reader = new BufferedReader(new InputStreamReader(stream));
//            } else if (s3Key.endsWith(".gz")) {
//                GZIPInputStream gzipInputStream = new GZIPInputStream(objectContent);
//                reader = new BufferedReader(new InputStreamReader(gzipInputStream));
//            } else {
//                reader = new BufferedReader(new InputStreamReader(objectContent));
//            }
//            while (true) {
//                String line;
//                try {
//                    line = reader.readLine();
//                    if (line == null) break;
//                    System.out.println("\n" + line);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("ok!断点续传标记数:"+i);
//            try {
//                reader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Test
    public void test_JSONArray()
    {
        List<String> list = new ArrayList<>();
        list.add("123");
        list.add("234");
        list.add("345");
        JSONArray jsonArray = JSONArray.fromObject(list);
        System.out.println(jsonArray);
    }

    @Test
    public void testFloat()
    {
        int a = 2;

        System.out.println((float) (1/(float)a));
    }

    @Test
    public void deleteFile()
    {
        File file = new File("/Users/xuejin/Desktop/S3/download/3.dmg");
        file.delete();
    }

    @Test
    public void UserInfo()
    {
        String accessKey = "98SRU6JLWGSPCLZ9UVR4";
        String secretKey = "cMI813ADNAPaSQSw0spbEgv3vIDdilPFsLn5MCFe";
        String adminEndpoint = "http://192.168.2.2:1999/admin";
        RgwAdmin rgwAdmin = new RgwAdminBuilder().accessKey(accessKey).secretKey(secretKey).endpoint(adminEndpoint)
                .build();

        System.out.println(rgwAdmin.getUserQuota("薛进").get().getMaxSizeKb());
        System.out.println(rgwAdmin.getUserInfo("薛进").get().getMaxBuckets());//用户最多桶的个数

        rgwAdmin.setIndividualBucketQuota("薛进","test-abdae0343fc1491a972b201b5db0e827",100,1024*1024*1);

        System.out.println(rgwAdmin.getBucketInfo("test-abdae0343fc1491a972b201b5db0e827").get().getBucketQuota().getMaxSizeKb());
        //System.out.println(rgwAdmin.getBucketInfo("success-2a7fdb4e9cd94b52b7e7f705c6884cbe").get().getUsage().getRgwMain().getNum_objects());
        //rgwAdmin.setUserQuota("薛进",100,5*1024*1024);
    }

    @Test
    public void test_view() throws FileNotFoundException {
        S3ServiceImpl s3Service = new S3ServiceImpl();
        AmazonS3 s3 = s3Service.getS3Client("7METYYEX1K0DDY3E0ODM","EAuk8pFbeA8i3pNnPY2x68FDXmI452RyMdRZUwse");
        //s3.putObject(new PutObjectRequest("hello-2bddbd0ab6564a5bacb7b6f7df40c2ff","test.png","/Users/xuejin/Desktop/S3/download/1.png").withCannedAcl(CannedAccessControlList.PublicRead));
//
//        ObjectMetadata objectMetadata = new ObjectMetadata();
//        objectMetadata.setContentType("application/pdf");

        //File file = new File("/Users/xuejin/Desktop/S3/download/1.png");
//        InputStream inputStream = new FileInputStream(new File("/Users/xuejin/Downloads/2016C语言真题.pdf"));
//
//        s3.putObject(new PutObjectRequest("hello-2bddbd0ab6564a5bacb7b6f7df40c2ff", "test.pdf", inputStream, objectMetadata));
//
        ObjectMetadata objectMetadata1 = s3.getObjectMetadata("hello-2bddbd0ab6564a5bacb7b6f7df40c2ff","s3管理用户权限.png");

        System.out.println(objectMetadata1.getContentType());
        //设置过期时间 9700小时
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * 60 * 24 * 7;
        expiration.setTime(expTimeMillis);
        //获取一个request
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                "hello-2bddbd0ab6564a5bacb7b6f7df40c2ff", "s3管理用户权限.png").withExpiration(expiration);
        //生成公用的url
        URL url = s3.generatePresignedUrl(urlRequest);
        System.out.println(url.toString());
    }

    @Test
    public void findUser()
    {
        String i = "123";
        System.out.println(i.contentEquals("123"));
    }

    @Test
    public void DateMinus() throws ParseException {
        Date date = new Date();
        Date date1=null;
        SimpleDateFormat simdate1=new SimpleDateFormat("yyyy-MM-dd");
        String str1="2020-4-11";
        Date paste = simdate1.parse(str1);
        System.out.println((date.getTime()-paste.getTime())/ (1000 * 60 * 60 *24));
    }

    @Test
    public void test_while_do(){
        char i ='0';
        do{
            System.out.println(i++);
        }while (i!='9');
        String s = "ok";
        System.out.println(s.equals("ok"));
    }

    @Test
    public void test_1()
    {
        List<FileRecover> list = fileService.getDeleteFileList("Xiaoming");
        list.forEach(System.out::println);
    }

    @Test
    public void  testCreateBucket(){
        AWSCredentials credentials = new BasicAWSCredentials("E8LH4Q64DSFHDAAM3JQN", "SNWENXYMywYBJzbgJw7DLgdPaziDO7fBWHo8nAmZ");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://cephinside.un-net.com",""))
                .withPathStyleAccessEnabled(true)
                .build();
        s3.createBucket("recover-123454231342323");
    }

    @Test
    public void listenUploadProgress(){
        AWSCredentials credentials = new BasicAWSCredentials("U6DVKQNYCPHCIIP0XZFJ", "A32aqcbXEECBtfxL2UqqBoMs2zyJJBMnRpG0gj7i");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://172.23.27.119:7480",""))
                .withPathStyleAccessEnabled(true)
                .build();

        File file = new File("/Users/cooper/Desktop/transporter.zip");
        TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3).build();
        // For more advanced uploads, you can create a request object
        // and supply additional request parameters (ex: progress listeners,
        // canned ACLs, etc.)
        PutObjectRequest request = new PutObjectRequest(
                "Aaa", "upload.zip", file);

        // You can ask the upload for its progress, or you can
        // add a ProgressListener to your request to receive notifications
        // when bytes are transferred.
//        request.setGeneralProgressListener(new com.amazonaws.event.ProgressListener() {
//            @Override
//            public void progressChanged(ProgressEvent progressEvent) {
//                System.out.println("Transferred bytes: " +
//                        progressEvent.getBytesTransferred());
//            }
//        });

        // TransferManager processes all transfers asynchronously,
        // so this call will return immediately.
        Upload upload = tm.upload(request);
        XferMgrProgress.showTransferProgress(upload);
        XferMgrProgress.waitForCompletion(upload);
//        try {
//            // You can block and wait for the upload to finish
//            upload.waitForCompletion();
//        } catch (AmazonClientException | InterruptedException amazonClientException) {
//            System.out.println("Unable to upload file, upload aborted.");
//            amazonClientException.printStackTrace();
//        }
    }
}
