package com.cooper.demo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cooper.demo.Bean.User;
import com.cooper.demo.Mapper.UserMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 薛进
 * @version 1.0
 * @Description
 * @date 2021/6/14 4:36 下午
 */
@SpringBootTest
public class MybatisPlusTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void checkUserIfExists(){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("username","Xiaoming");
        User user = userMapper.selectOne(wrapper);
        if(user==null){
            System.out.println("null");
        }
        System.out.println(user.toString());
    }
}
