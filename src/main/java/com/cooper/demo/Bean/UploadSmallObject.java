package com.cooper.demo.Bean;

import net.sf.json.JSONObject;

public class UploadSmallObject {
    private String fileName;
    private String fileUploadProgress;

    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName",fileName);
        jsonObject.put("fileUploadProgress",fileUploadProgress);
        return jsonObject;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUploadProgress() {
        return fileUploadProgress;
    }

    public void setFileUploadProgress(String fileUploadProgress) {
        this.fileUploadProgress = fileUploadProgress;
    }

    @Override
    public String toString() {
        return "UploadSmallObject{" +
                "fileName='" + fileName + '\'' +
                ", fileUploadProgress='" + fileUploadProgress + '\'' +
                '}';
    }
}
