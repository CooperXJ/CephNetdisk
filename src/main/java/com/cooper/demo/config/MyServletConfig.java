package com.cooper.demo.config;

import com.cooper.demo.service.S3.S3_Stream_upload;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyServletConfig {

    @Bean
    public ServletRegistrationBean myServlet(){

        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new S3_Stream_upload(),"/user/file/upload");
        return registrationBean;
    }

}
