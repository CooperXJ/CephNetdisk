package com.cooper.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//MapperScan注解使得该包下面的所有Mapper类都可以获得@Mapper注解
@MapperScan("com.cooper.demo.Mapper")

//开启缓存
@EnableCaching
public class NetdiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(NetdiskApplication.class, args);
    }

}
