package com.cooper.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import com.cooper.demo.MyDownloadCallable.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

import static com.amazonaws.services.s3.internal.ServiceUtils.createParentDirectoryIfNecessary;


/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/4/4 2:12 下午
 */
@SpringBootTest
@Slf4j
public class UploadObject {
    @BeforeClass
    public static void beforeClass()
    {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    static AWSCredentials credentials = new BasicAWSCredentials("U6DVKQNYCPHCIIP0XZFJ", "A32aqcbXEECBtfxL2UqqBoMs2zyJJBMnRpG0gj7i");

    static AmazonS3 s3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://172.23.27.119:7480",""))
            .withPathStyleAccessEnabled(true)
            .build();
    static TransferManager tm = TransferManagerBuilder.standard().withS3Client(s3).build();

    static ExecutorService service = Executors.newFixedThreadPool(10);

    public static Upload main() throws InterruptedException {
        File file = new File("/Users/cooper/Desktop/upload/transporter.zip");
        File file1 = new File("/Users/cooper/Desktop/demo.zip");

//        MultipleFileUpload multipleFileUpload = tm.uploadDirectory("Aaa", "folder", new File("/Users/cooper/Desktop/upload"), true);
        // For more advanced uploads, you can create a request object
        // and supply additional request parameters (ex: progress listeners,
        // canned ACLs, etc.)
        ObjectMetadata metadata = new ObjectMetadata();
        Map map = new HashMap();
        map.put("x-amz-mp-parts-count","6");
        metadata.setUserMetadata(map);
        PutObjectRequest request = new PutObjectRequest(
                "Aaa", "upload.zip", file).withMetadata(metadata);

        Upload upload = tm.upload(request);
        upload.pause();
//        XferMgrProgress.showTransferProgress(upload);
//        XferMgrProgress.waitForCompletion(upload);
        Thread.sleep(1000*5);
        return upload;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        PauseResult<PersistableUpload> persistableUploadPauseResult = main().tryPause(true);
//        System.out.println(persistableUploadPauseResult.getPauseStatus().toString());
//        PersistableUpload infoToResume = persistableUploadPauseResult.getInfoToResume();
//        System.out.println(infoToResume.toString());
//        tm.resumeUpload(infoToResume);

//        GetObjectRequest request = new GetObjectRequest("Aaa","upload.zip" );
//        Download aaa = tm.download(request, new File("/Users/cooper/Desktop/upload/download/download.zip"));
//
//        Thread.sleep(1000*2);
//
//        PersistableDownload pause = aaa.pause();
//        tm.resumeDownload(pause);


        UploadObject object = new UploadObject();
        MyDownloadImpl myDownload = object.multiPartDownload();

        Thread.sleep(1000*2);

        Map map = myDownload.pause();

        for(Object key:map.keySet()){
            System.out.println((map.get(key)).toString());
        }

        object.resumeDownload(map,new File("/Users/cooper/Desktop/upload/download/test.zip"),myDownload);
    }


    @Test
    public void downloadFile() throws InterruptedException {
        GetObjectRequest request = new GetObjectRequest("Aaa","upload.zip" );
        Download aaa = tm.download(request, new File("/Users/cooper/Desktop/upload/download/download.zip"));
        aaa.waitForCompletion();
//        Thread.sleep(1000*2);
//
//        PersistableDownload pause = aaa.pause();
//        tm.resumeDownload(pause);
//        System.out.println(aaa.getProgress().getBytesTransferred());
    }

    @Test
    public void setMeteDate(){
//        s3.getObjectMetadata("Aaa","upload.zip").getUserMetadata().forEach(new BiConsumer<String, String>() {
//            @Override
//            public void accept(String s, String s2) {
//                System.out.println("***********"+s+"  "+s2);
//            }
//        });

        GetObjectMetadataRequest request = new GetObjectMetadataRequest("Aaa","upload.zip");
        System.out.println("get part number "+request.getBucketName());
        ObjectMetadata objectMetadata = s3.getObjectMetadata(request);
        System.out.println(objectMetadata.getUserMetaDataOf("x-amz-mp-parts-count"));
    }

    @Test
    public void download() throws InterruptedException {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK,1);
        URL url = s3.generatePresignedUrl("Aaa", "upload.zip", calendar.getTime());
        PresignedUrlDownloadRequest request = new PresignedUrlDownloadRequest(url);
        PresignedUrlDownloadConfig config = new PresignedUrlDownloadConfig();
        config.withDownloadSizePerRequest(1024*1024*10);//默认5M为一个chunk，现在改成10M
//        config.withResumeOnRetry(true);
        PresignedUrlDownload download = tm.download(request, new File("/Users/cooper/Desktop/upload/download/test.zip"), config);
//        download.getProgress().getBytesTransferred();
        download.waitForCompletion();
    }

    public MyDownloadImpl multiPartDownload() throws InterruptedException, ExecutionException {
        long length = s3.getObjectMetadata("Aaa", "upload.zip").getContentLength()-1;
        GetObjectRequest request = new GetObjectRequest("Aaa","upload.zip");

        long bufferSize = 1024*1024*10;
        int partCount = (int)Math.ceil((double)length/bufferSize);//必须使用double，否则会变成整数除法
        CountDownLatch countDownLatch = new CountDownLatch(1);
        long start = 0;


        String fileName = "/Users/cooper/Desktop/upload/download/test.zip";
        File file = new File(fileName);

        TransferProgress progress = new TransferProgress();
        progress.setTotalBytesToTransfer(length);
        MyDownloadImpl myDownload = new MyDownloadImpl(progress,partCount,start,length,bufferSize);
        MyMonitor myMonitor = new MyMonitor();

        service.submit(new newDownloadS3ObjectCallable(file,myDownload,length,bufferSize,request,countDownLatch,service));

        myDownload.setMonitor(myMonitor);

        countDownLatch.countDown();
        return myDownload;
    }

    class MyCallableThread implements Callable<Integer>{
        private AmazonS3 s3;
        private long start;
        private long end;
        private String fileName;
        private Integer partNumber;
        private CountDownLatch countDownLatch;
        private String bucketName;
        private String keyName;
        public MyCallableThread( AmazonS3 s3, long start, long end, String fileName, Integer partNumber,CountDownLatch countDownLatch,
                                 String bucketName,String keyName) {
            this.s3 = s3;
            this.start = start;
            this.end = end;
            this.fileName = fileName;
            this.partNumber = partNumber;
            this.countDownLatch = countDownLatch;
            this.bucketName = bucketName;
            this.keyName = keyName;
        }

        @Override
        public Integer call() throws Exception {
            GetObjectRequest request = new GetObjectRequest(bucketName,keyName);
            request.setRange(start,end);
            log.error("download range  "+start+"  "+end);
            s3.getObject(request, new File(fileName));
            countDownLatch.countDown();
            return partNumber;
        }
    }

    public Callable<S3ObjectInputStream> getPartObject(GetObjectRequest request){
        return new Callable<S3ObjectInputStream>() {
            @Override
            public S3ObjectInputStream call() throws Exception {
                S3ObjectInputStream objectContent = s3.getObject(request).getObjectContent();
                return objectContent;
            }
        };
    }

    @Test
    public void testCeil(){
        String fileName = "/Users/cooper/Desktop/upload/download/test";
        GetObjectRequest request = new GetObjectRequest("Aaa","upload.zip");
        request.setRange(0 ,10485760);
        s3.getObject(request, new File(fileName+1));
        request.setRange(10485761 ,20971521);
        s3.getObject(request, new File(fileName+2));
        request.setRange(20971522 ,27072330);
        s3.getObject(request, new File(fileName+3));
    }

    @Data
    class newDownloadS3ObjectCallable implements Callable<File>{
        private final Log LOG = LogFactory.getLog(newDownloadS3ObjectCallable.class);
        private final File destinationFile;
        private MyDownloadImpl myDownload;
        private CountDownLatch countDownLatch;
        private final Long length;
        private Long start = 0L;
        private Long end = 0L;
        private final Long bufferSize;
        private final GetObjectRequest request;
        private Long filePositionToWrite = 0L;
        private ExecutorService service;
        private boolean isResumeDownload;

        public newDownloadS3ObjectCallable(File destinationFile, MyDownloadImpl myDownload, Long length,Long bufferSize, GetObjectRequest request, CountDownLatch latch,ExecutorService service) {
            this.destinationFile = destinationFile;
            this.myDownload = myDownload;
            this.length = length;
            this.bufferSize = bufferSize;
            this.request = request;
            this.countDownLatch = latch;
            this.service = service;
        }

        @Override
        public File call() throws Exception {
            countDownLatch.await();

            createParentDirectoryIfNecessary(destinationFile);
//            //文件加锁
//            if (!FileLocks.lock(destinationFile)) {
//                throw new FileLockException("Fail to lock " + destinationFile);
//            }

            //设置任务列表，暂停任务时需要使用
            List<Future<Long>> futures = new ArrayList<Future<Long>>();

            int i = 0;
            while(start<=length){
                end = start + bufferSize - 1;

                if (end > length) {
                    end = length;
                }

                GetObjectRequest rangeRequest = (GetObjectRequest) request.clone();
                rangeRequest.setRange(start, end);
                Callable<S3ObjectInputStream> partObject = getPartObject(rangeRequest);
                Future<Long> future = service.submit(new DownloadS3ObjectCallable(destinationFile, filePositionToWrite, partObject, i, myDownload));
                futures.add(future);

                filePositionToWrite += bufferSize;
                start = end+1;
                i++;
            }
            //设置任务队列
            myDownload.getMonitor().setFuture(new MyCompositeFuture<>(futures));

            return destinationFile;
        }
    }

    public void resumeDownload(Map<Integer,DownloadLog> map,File destinationFile,MyDownloadImpl myDownload){
        GetObjectRequest request = new GetObjectRequest("Aaa","upload.zip");
        long nowDownloaded = map.values().stream().mapToLong(s -> (s.getCurpos() - s.getStart())).sum();
        myDownload.getProgress().setTotalBytesToTransfer(myDownload.getSize()-nowDownloaded);
        //设置任务列表，暂停任务时需要使用
        List<Future<Long>> futures = new ArrayList<Future<Long>>();
        map.entrySet().forEach(s->{
            DownloadLog value = s.getValue();
            if(value.getCurpos()<value.getEnd()){
                GetObjectRequest rangeRequest = (GetObjectRequest) request.clone();
                rangeRequest.setRange(value.getCurpos(),value.getEnd());
                Callable<S3ObjectInputStream> partObject = getPartObject(rangeRequest);
                Future<Long> future = service.submit(new DownloadS3ObjectCallable(destinationFile,value.getCurpos(), partObject,s.getKey(), myDownload));
                futures.add(future);
            }
        });
        //设置任务队列
        myDownload.getMonitor().setFuture(new MyCompositeFuture<>(futures));
    }


    @Test
    public void testAmazonV2() throws URISyntaxException, IOException {
        AwsBasicCredentials provider = AwsBasicCredentials.create("U6DVKQNYCPHCIIP0XZFJ", "A32aqcbXEECBtfxL2UqqBoMs2zyJJBMnRpG0gj7i");
        S3Client client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(provider))
                .endpointOverride(new URI("http://172.23.27.119:7480")).region(Region.AWS_CN_GLOBAL).build();
        software.amazon.awssdk.services.s3.model.PutObjectRequest objectRequest = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                .bucket("Aaa")
                .key("new Aws sdk")
                .build();
        client.putObject(objectRequest, RequestBody.fromByteBuffer(getRandomByteBuffer(10_000)));

    }

    private ByteBuffer getRandomByteBuffer(int size) throws IOException {
        byte[] b = new byte[size];
        new Random().nextBytes(b);
        return ByteBuffer.wrap(b);
    }

    @Test
    public void testProcessInProcess(){
        Runnable r1 = ()->{
            System.out.println("父线程正在执行   "+Thread.currentThread().getName());
            for(int i =0;i<10;i++){
                Runnable r = ()->{
                    System.out.println("子线程正在执行   "+Thread.currentThread().getName());
                };
                Thread thread = new Thread(r,String.valueOf(i));
                thread.start();
            }
        };

        Thread thread = new Thread(r1,"-1");
        thread.start();
    }

    @Test
    public void testUpload(){
        File file = new File("/Users/cooper/Desktop/upload/transporter.zip");
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

        List<PartETag> partETags = new ArrayList<PartETag>();

        String bucketName = "Aaa";
        String keyName = "upload.zip";
        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, keyName);
        InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);

        // Upload the file parts.
        long filePosition = 0;
        for (int i = 1; filePosition < contentLength; i++) {
            // Because the last part could be less than 5 MB, adjust the part size as needed.
            partSize = Math.min(partSize, (contentLength - filePosition));

            // Create the request to upload a part.
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(bucketName)
                    .withKey(keyName)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(partSize);

            // Upload the part and add the response's ETag to our list.
            UploadPartResult uploadResult = s3.uploadPart(uploadRequest);
            partETags.add(uploadResult.getPartETag());

            filePosition += partSize;
        }

        // Complete the multipart upload.
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, keyName,
                initResponse.getUploadId(), partETags);
        s3.completeMultipartUpload(compRequest);
    }

    @Test
    public void compareFile() throws IOException {
        FileInputStream inputStream1 = new FileInputStream(new File("/Users/cooper/Desktop/upload/download/test.zip"));
        FileInputStream inputStream2 = new FileInputStream(new File("/Users/cooper/Desktop/upload/download/download.zip"));
        byte[] buffer1 = new byte[1024];
        byte[] buffer2 = new byte[1024];
        int count = 0;

        while(inputStream1.read(buffer1)!=0&&inputStream2.read(buffer2)!=0){
            if(!Arrays.equals(buffer1,buffer2)){
                count++;
            }
        }

        System.out.println(count);
        inputStream1.close();
        inputStream2.close();
    }

    @Test
    public void  process() throws InterruptedException {
        letterCombinations("23").forEach(System.out::println);
    }

    Map<Integer,String> map = new HashMap<Integer,String>(){{
        put(2,"abc");
        put(3,"def");
        put(4,"ghi");
        put(5,"jkl");
        put(6,"mno");
        put(7,"pqrs");
        put(8,"tuv");
        put(9,"wxyz");
    }};
    public List<String> letterCombinations(String digits) {
        int[] arr = new int[digits.length()];

        for(int i =0;i<digits.length();i++){
            arr[i] = digits.charAt(i)-'0';
        }
        int count = 0;
        Queue<String> queue = new LinkedList();
        queue.offer(map.get(arr[count++]));
        List<String> out = new ArrayList<>();

        int appendCount = 0;
        String res = "";
        while(!queue.isEmpty()){
            String temArr = queue.poll();
            for(char ch:temArr.toCharArray()){
                res+=ch;
                if(appendCount==digits.length()){
                    out.add(res);
                    res = "";
                    appendCount = 0;
                    break;
                }
                queue.offer(map.get(arr[count]));
            }
            count++;
            appendCount++;
        }

        return out;
    }
}
