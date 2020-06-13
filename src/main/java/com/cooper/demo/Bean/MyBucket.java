package com.cooper.demo.Bean;

import com.amazonaws.services.s3.model.Bucket;
import com.cooper.demo.service.S3.S3ServiceImpl;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.util.Date;

public class MyBucket implements Serializable {

    private String myBucketName;
    //完整的名称
    private String myBucketFullName;

    private S3ServiceImpl s3Service;
    private Bucket bucket;
    private Date createDate;

    public MyBucket(Bucket bucket,S3ServiceImpl s3Service)
    {
        this.s3Service = s3Service;
        this.bucket = bucket;
    }

    public S3ServiceImpl getS3Service() {
        return s3Service;
    }

    public void setS3Service(S3ServiceImpl s3Service) {
        this.s3Service = s3Service;
    }

    public String getMyBucketName() {
        myBucketName = s3Service.getBucketName(bucket.getName());
        return myBucketName;
    }

    public void setMyBucketName(String myBucketName) {
        this.myBucketName = myBucketName;
    }

    public String getMyBucketFullName() {
        return bucket.getName();
    }

    public void setMyBucketFullName(String myBucketFullName) {
        this.myBucketFullName = myBucketFullName;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public Date getCreateDate() {
        return bucket.getCreationDate();
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "MyBucket{" +
                "myBuckentName='" + myBucketName + '\'' +
                ", myBucketFullName='" + myBucketFullName + '\'' +
                ", s3Service=" + s3Service +
                ", bucket=" + bucket +
                ", createDate=" + createDate +
                '}';
    }
}
