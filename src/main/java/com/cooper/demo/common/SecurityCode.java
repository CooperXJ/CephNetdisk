package com.cooper.demo.common;

/**
 * @Descriptuion TODO 获取一个六位数 即用户重置密码时用到的激活码
 **/
public class SecurityCode {

    public static Integer GetSecurityCode(){
        int a=(int)(Math.random()*1000000);
        return a;
    }
}
