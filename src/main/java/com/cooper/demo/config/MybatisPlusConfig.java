package com.cooper.demo.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author cy
 * @Description
 * @Date 2020/9/27
 */
@Configuration
@MapperScan("com.cooper.demo.Mapper*")
public class MybatisPlusConfig {

    /**
     *  mybatis-plus分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor page = new PaginationInterceptor();
        page.setDialectType(DbType.MYSQL.getDb());
        return page;
    }
}
