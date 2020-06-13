package com.cooper.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.io.File;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MailTest {
    @Autowired
    JavaMailSenderImpl mailSender;

    //以下使用两种不同的方法测试邮件的发送

    //简单文本邮件
    @Test
    public void contextLoads(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("今晚开会");
        message.setText("大家，好！\n今晚7:30在教学楼201开班委会，请各位班委准时参加！ \n谢谢！");
        message.setTo("1789023580@qq.com");
        message.setFrom("1789023580@qq.com");

        mailSender.send(message);
    }

    //多媒体邮件（支持html和附件等）
    @Test
    public void test2() throws Exception{
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
        helper.setSubject("今晚开会");
        helper.setText("大家，好！<br> &nbsp;&nbsp;<b style='color:red'>今晚7:30在教学楼201开班委会，请各位班委准时参加！</b> <br>谢谢！",true);
        helper.setTo("1686004962@qq.com");
        helper.setFrom("1789023580@qq.com");

        //添加附件
        helper.addAttachment("会议说明.txt",new File("C:\\Users\\Aaron\\Desktop\\s3\\1.txt"));
        //helper.addAttachment("会议图片.jpg",new File("C:\\Users\\Dylan\\Pictures\\会议图片.jpg"));

        mailSender.send(mimeMessage);
    }


}
