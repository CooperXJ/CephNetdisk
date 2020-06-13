package com.cooper.demo.Bean;

public class Friend {

    private String user_id;
    private String user_nick_name;
    private String user_header_img;
    private String friend_id;
    private String friend_nick_name;
    private String friend_header_img;
    private String ctime;
    private String reason;
    private Integer isaccept;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    private Integer notread_num;

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUser_nick_name() {
        return user_nick_name;
    }

    public void setUser_nick_name(String user_nick_name) {
        this.user_nick_name = user_nick_name;
    }

    public String getUser_header_img() {
        return user_header_img;
    }

    public void setUser_header_img(String user_header_img) {
        this.user_header_img = user_header_img;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public String getFriend_nick_name() {
        return friend_nick_name;
    }

    public void setFriend_nick_name(String friend_nick_name) {
        this.friend_nick_name = friend_nick_name;
    }

    public String getFriend_header_img() {
        return friend_header_img;
    }

    public void setFriend_header_img(String friend_header_img) {
        this.friend_header_img = friend_header_img;
    }

    public Integer getIsaccept() {
        return isaccept;
    }

    public void setIsaccept(Integer isaccept) {
        this.isaccept = isaccept;
    }

    public Integer getNotread_num() {
        return notread_num;
    }

    public void setNotread_num(Integer notread_num) {
        this.notread_num = notread_num;
    }
}
