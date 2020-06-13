package com.cooper.demo.service.S3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.commons.fileupload.FileItem;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

public interface S3Service {
    //下载
    public void downloadFromS3(AmazonS3 s3Client,String bucketName,String key,String targetFIlePath);

    //上传
    public void uploadToS3(AmazonS3 s3Client, String bucketName, FileItem tempFile, String keyName, ObjectMetadata objectMetadata)throws IOException;

    //创建桶
    public Bucket createBucket(AmazonS3 s3Client,String bucketName)throws SocketTimeoutException;

    //查看桶
    public List getBuckets(AmazonS3 s3Client);

    //根据桶查看里面的文件
    public List getFilesFromBucket(AmazonS3 s3Client,String bucketName);

    //根据桶查看里面的文件（根据前缀）
    public List getFilesFromBucket(AmazonS3 s3Client,String bucketName,String prefix);

    //删除文件
    public boolean deleteFile(AmazonS3 s3Client,String bucketName,String fileName);

    //删除桶
    public boolean deleteBucket(AmazonS3 s3Client,String bucketName)throws SocketTimeoutException;

    //查看桶是否存在
    public boolean isBucketExists(AmazonS3 s3Client,String bucketName)throws SocketTimeoutException;

    //查看当前桶下指定文件是否存在
    public boolean isFileExits(AmazonS3 s3Client,String bucketName,String fileName)throws  SocketTimeoutException;

    //获得指定的用户对用的S3Client
    public AmazonS3 getS3Client(String AWS_ACCESS_KEY,String AWS_SECRET_KEY);
}
