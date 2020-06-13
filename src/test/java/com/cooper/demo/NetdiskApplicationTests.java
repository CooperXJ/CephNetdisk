package com.cooper.demo;

import com.cooper.demo.Bean.User;
import com.cooper.demo.service.User.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@SpringBootTest
class NetdiskApplicationTests {

    @Autowired
    DataSource dataSource;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

}
