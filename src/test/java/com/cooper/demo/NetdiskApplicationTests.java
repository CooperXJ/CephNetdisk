package com.cooper.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cooper.demo.Bean.User;
import com.cooper.demo.Mapper.UserMapper;
import com.cooper.demo.service.Chat.UserService;
import com.cooper.demo.service.User.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NetdiskApplicationTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Value("${ceph.endpoint}")
    private String endpoint;

    @Value("${ceph.ak}")
    private String ak;

    @Value("${ceph.sk}")
    private String sk;

    @Value("${ceph.picBucket}")
    private String picBucket;

    @Test
    void contextLoads() {
//        User user = userMapper.getUserByID(1);
//        System.out.println(user);
    }

    @Test
    void Test_MySQL_connection() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

    @Test
    void Test_show_UserList()
    {
        List<User> userList = userService.getAllUser();
        for(User user:userList)
        {
            System.out.println(user);
        }
    }

    @Test
    void Test_update_SQL()
    {
        String sql = "update test.user set username=? , password=?, email=? , active_status=? where id=?";
        jdbcTemplate.update(sql,"薛进","123456","1789023580@qq.com",1,2);
    }

     @Test
     void checkUserIfExists() {
         QueryWrapper wrapper = new QueryWrapper();
         wrapper.eq("username", "Xiaoming");
         User user = userMapper.selectOne(wrapper);
         if (user == null) {
             System.out.println("null");
         }
         System.out.println(user.toString());
     }

     @Test
    void makeBucketPublic(){
         AWSCredentials credentials = new BasicAWSCredentials(ak,sk);
         AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                 .withCredentials(new AWSStaticCredentialsProvider(credentials))
                 .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint,""))
                 .withPathStyleAccessEnabled(true)
                 .build();

     }
}
