package com.cooper.demo.Bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Data
@ToString
//Serializable的作用
/*
    1.序列化：把对象转换为字节序列的过程称为对象的序列化。
    反序列化：把字节序列恢复为对象的过程称为对象的反序列化。
    2.使用场景
    当你想把的内存中的对象状态保存到一个文件中或者数据库中时候；
    当你想用套接字在网络上传送对象的时候；
    当你想通过RMI传输对象的时候；
 */
@TableName(value = "user")
public class User implements Serializable {

    @TableId
    private Integer id;
    private String username;
    private String nick_name;
    private String header_img;
    private String password;
    private String email;
    // 激活状态 0 未激活 1 已激活
    private Integer active_status;
    // 激活码
    @TableField(value = "active_code")
    private String activeCode;

    //重置密码时的验证码
    @TableField(value = "security_code")
    private Integer SecurityCode;

    //客户的Access_key
    @TableField(value = "access_key")
    private String access_key;

    //客户的Secret_key
    @TableField(value = "secret_key")
    private String secret_key;

    //客户的存储空间
    @TableField(value = "storage_space")
    private Integer StorageSpace;

    //客户存储的最大数量
    @TableField(value = "max_storage_number")
    private Integer MaxStorageNumber;

    //用户恢复库
    @TableField(value = "recover_bucket")
    private String recoverBucket;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getActive_status() {
        return active_status;
    }

    public void setActive_status(Integer active_status) {
        this.active_status = active_status;
    }

    public String getActiveCode() {
        return activeCode;
    }

    public void setActiveCode(String activeCode) {
        this.activeCode = activeCode;
    }

    public void setSecurityCode(Integer securityCode) {
        SecurityCode = securityCode;
    }

    public Integer getSecurityCode() {
        return SecurityCode;
    }

    public String getAccess_key() {
        return access_key;
    }

    public void setAccess_key(String access_key) {
        this.access_key = access_key;
    }

    public String getSecret_key() {
        return secret_key;
    }

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }

    public Integer getStorageSpace() {
        return StorageSpace;
    }

    public void setStorageSpace(Integer storageSpace) {
        StorageSpace = storageSpace;
    }

    public Integer getMaxStorageNumber() {
        return MaxStorageNumber;
    }

    public void setMaxStorageNumber(Integer maxStorageNumber) {
        MaxStorageNumber = maxStorageNumber;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getHeader_img() {
        return header_img;
    }

    public void setHeader_img(String header_img) {
        this.header_img = header_img;
    }

    public String getRecoverBucket() {
        return recoverBucket;
    }

    public void setRecoverBucket(String recoverBucket) {
        this.recoverBucket = recoverBucket;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", header_img='" + header_img + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", active_status=" + active_status +
                ", activeCode='" + activeCode + '\'' +
                ", SecurityCode=" + SecurityCode +
                ", access_key='" + access_key + '\'' +
                ", secret_key='" + secret_key + '\'' +
                ", StorageSpace=" + StorageSpace +
                ", MaxStorageNumber=" + MaxStorageNumber +
                ", recoverBucket='" + recoverBucket + '\'' +
                '}';
    }
}
