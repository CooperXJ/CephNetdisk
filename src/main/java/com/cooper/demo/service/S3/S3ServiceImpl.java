package com.cooper.demo.service.S3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.cooper.demo.common.IDUtils;
import org.apache.commons.fileupload.FileItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3ServiceImpl implements S3Service {

    @Value("${ceph.endpoint}")
    private String ENDPOINT ;//此处端口进行了映射

//    private static AmazonS3 s3Client;

    //初始化s3的连接对象s3Client

//    static {
//        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY,AWS_SECRET_KEY);
//        s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT,"")).build();
//        //测试是否连接上去了
//        System.out.println("||| buckets： "+s3Client.listBuckets()+"\n");
//    }

    //获得s3的客户端
    @Override
    public AmazonS3 getS3Client(String AWS_ACCESS_KEY, String AWS_SECRET_KEY) {
        AWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY,AWS_SECRET_KEY);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ENDPOINT,"")).build();

        return s3Client;
    }

    @Override
    public void downloadFromS3(AmazonS3 s3Client,String bucketName,String key,String targetFIlePath){
        bucketName = getTargetBucket(s3Client,bucketName);

        S3Object object = s3Client.getObject(new GetObjectRequest(bucketName,key));
        if(object!=null)
        {
            //System.out.println("Content-Type: "+object.getObjectMetadata().getContentType());
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            byte[] data = null;

            try{
                inputStream = object.getObjectContent();
                data = new byte[inputStream.available()];
                int len = 0;
                fileOutputStream = new FileOutputStream(targetFIlePath);
                while ((len=inputStream.read(data))!=-1)
                {
                    fileOutputStream.write(data,0,len);
                }
                System.out.println("文件下载成功！");
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(fileOutputStream!=null)
                {
                    try {
                        fileOutputStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //上传文件
    //此处的key是唯一标识符，是key-value中的key
    @Override
    public void uploadToS3(AmazonS3 s3Client, String bucketName, FileItem tempFile, String keyName, ObjectMetadata metadata)throws IOException
    {
        try {
            //bucketName = getTargetBucket(s3Client,bucketName);
            InputStream inputStream = tempFile.getInputStream();
            PutObjectRequest request = new PutObjectRequest(bucketName,keyName,inputStream,metadata);
            s3Client.putObject(request);
            System.out.println("文件上传成功！");
        }catch (AmazonServiceException e)
        {
            e.printStackTrace();
        }catch (AmazonClientException e)
        {
            e.printStackTrace();
        }
    }


    //创建桶
    @Override
    public Bucket createBucket(AmazonS3 s3Client,String bucketName) throws SocketTimeoutException {

        //这里必须是小写否则会出现错误   并且名称有规范 具体参考：https://www.crifan.com/aws_s3_bucket_naming_rule/
        //名称需要唯一
        bucketName = bucketName.toLowerCase()+"-"+IDUtils.getUUID();
        Bucket bucket=s3Client.createBucket(bucketName);
        return  bucket;
    }

    @Override
    public List<Bucket> getBuckets(AmazonS3 s3Client) {
            List<Bucket> buckets_List = s3Client.listBuckets();
            return buckets_List;
    }

    @Override
    public List<S3ObjectSummary> getFilesFromBucket(AmazonS3 s3Client,String bucketName) {
        List<S3ObjectSummary> list=new ArrayList();

        bucketName = getTargetBucket(s3Client,bucketName);

        ObjectListing objects = s3Client.listObjects(bucketName);

        int i=0;

        do {

            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {

                list.add(i,objectSummary);

                i++;

            }

            objects = s3Client.listNextBatchOfObjects(objects);

        } while (objects.isTruncated());

        return list;
    }


    @Override
    public List getFilesFromBucket(AmazonS3 s3Client,String bucketName,String prefix) {
        List list=new ArrayList();

        bucketName = getTargetBucket(s3Client,bucketName);

        ObjectListing objects = s3Client.listObjects(bucketName,prefix);

        int i=0;

        do {

            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {

                list.add(i,objectSummary);

                i++;

            }

            objects = s3Client.listNextBatchOfObjects(objects);

        } while (objects.isTruncated());

        return list;
    }

    @Override
    public boolean deleteFile(AmazonS3 s3Client,String bucketName, String fileName) {
        bucketName = getTargetBucket(s3Client,bucketName);
        s3Client.deleteObject(bucketName, fileName);
        return true;
    }

    @Override
    //删除目录，不为空时先将bucket里的所有对象删除再删除目录，否则会抛异常
    public boolean deleteBucket(AmazonS3 s3Client,String bucketName) throws SocketTimeoutException{
        if(this.getFilesFromBucket(s3Client,bucketName).isEmpty()){
            bucketName = getTargetBucket(s3Client,bucketName);
            s3Client.deleteBucket(bucketName);

        }else{
            String real_bucketName = getTargetBucket(s3Client,bucketName);

            //在进行文件查找的时候不需要对bucketName进行修改，因为getFilesFromBucket里面有方法处理bucketName
            List list=this.getFilesFromBucket(s3Client,bucketName);

            for(Object object:list){

                //这里需要真的bucketName
                s3Client.deleteObject(real_bucketName, ((S3ObjectSummary) object).getKey());

            }

            s3Client.deleteBucket(real_bucketName);
        }
        return true;
    }

    @Override
    public boolean isBucketExists(AmazonS3 s3Client,String bucketName) throws SocketTimeoutException {
        List<Bucket> list=this.getBuckets(s3Client);

        for(Bucket bucket:list){
            String str = getBucketName(bucket);

            if(str.equals(bucketName)){
                //System.out.println("已经存在该桶！");
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isFileExits(AmazonS3 s3Client,String bucketName, String fileName) throws SocketTimeoutException {

        List files=this.getFilesFromBucket(s3Client,bucketName);

        for(int i=0;i<files.size();i++) {
            S3ObjectSummary file = (S3ObjectSummary) files.get(i);
            //System.out.println("当前目录下的文件名：" + file.getFileName());
            if (fileName.equals(file.getKey())) {
                //System.out.println("已存在该文件！");
                return true;
            }
        }
        return  false;
    }


    //获取真实的bucketName
    public String getBucketName(Bucket bucket)
    {
        StringBuffer name = new StringBuffer();
        char[] bucket_name = bucket.getName().toCharArray();

        //唯一表示符号有32位，所以要删掉
        for(int i =bucket_name.length-34;i>=0;i--)
        {
            name.append(bucket_name[i]);
        }
        return name.reverse().toString();
    }

    //获得相应的Bucket
    String getTargetBucket(AmazonS3 s3Client,String bucketName)
    {
        List<Bucket> bucketList = getBuckets(s3Client);
        Bucket targetBucket = new Bucket();
        for(Bucket bucket:bucketList)
        {
            if(bucketName.equals(getBucketName(bucket)))
            {
                targetBucket = bucket;
                break;
            }
        }
        return targetBucket.getName();
    }

    //获取用户的bucketName
    public String getBucketName(String bucketName)
    {
        StringBuffer name = new StringBuffer();
        char[] bucket_name = bucketName.toCharArray();

        //唯一表示符号有32位，所以要删掉
        for(int i =bucket_name.length-34;i>=0;i--)
        {
            name.append(bucket_name[i]);
        }
        return name.reverse().toString();
    }
}
