package com.cooper.demo.Bean;

import net.sf.json.JSONObject;

public class UploadObject {
    private Integer order;
    private String fileName;
    private Integer is_done;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getIs_done() {
        return is_done;
    }

    public void setIs_done(Integer is_done) {
        this.is_done = is_done;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("order",order);
        jsonObject.put("fileName",fileName);
        jsonObject.put("progress",is_done);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "UploadObject{" +
                "order=" + order +
                ", fileName='" + fileName + '\'' +
                ", is_done=" + is_done +
                '}';
    }
}
