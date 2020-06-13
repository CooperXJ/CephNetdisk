package com.cooper.demo.Bean;

import net.sf.json.JSONObject;

public class RecoverFile {
    private String username;
    private String fileName;
    private String bucketName;
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
