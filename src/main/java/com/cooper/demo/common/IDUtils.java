package com.cooper.demo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class IDUtils {
    public static final Logger logger = LoggerFactory.getLogger(IDUtils.class);

    //获得唯一标识符
    public static String getUUID()
    {
        //1个UUID被连字符分为五段，形式为8-4-4-4-12的32个字符,此处是为了将“-”去除掉
        return UUID.randomUUID().toString().replace("-","");
    }

    public static void main(String[] args){
        System.out.println(getUUID());
        logger.debug("test");
    }
}
