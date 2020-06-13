package com.cooper.demo.Bean;

import net.sf.json.JSONObject;

public class UploadBigObject {
    private String fileName;
    private long fileSize;
    private long fileUploadProgress;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileUploadProgress() {
        return fileUploadProgress;
    }

    public void setFileUploadProgress(long fileUploadProgress) {
        this.fileUploadProgress = fileUploadProgress;
    }

    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName",fileName);
        jsonObject.put("fileSize",fileSize);
        jsonObject.put("fileUploadProgress",fileUploadProgress);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "UploadObject{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileUploadProgress=" + fileUploadProgress +
                '}';
    }
}
