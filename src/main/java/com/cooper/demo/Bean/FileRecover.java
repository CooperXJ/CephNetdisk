package com.cooper.demo.Bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import net.sf.json.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name = "file_recover")
@Table(name = "file_recover")
@TableName(value = "file_recover")
public class FileRecover implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String username;
    @Id
    @Column(name = "file_name")
    @TableField(value = "file_name")
    private String fileName;
    @Id
    @Column(name = "bucket_name")
    @TableField(value = "bucket_name")
    private String bucketName;
    @Column
    private String ctime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName",fileName);
        jsonObject.put("bucketName",bucketName);
        jsonObject.put("ctime",ctime);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "RecoverFile{" +
                "username='" + username + '\'' +
                ", fileName='" + fileName + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", ctime='" + ctime + '\'' +
                '}';
    }
}
