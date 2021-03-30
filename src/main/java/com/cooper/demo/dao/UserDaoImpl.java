package com.cooper.demo.dao;

import com.cooper.demo.Bean.User;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
/*
该注解是告诉Spring，让Spring创建一个名字叫“userDaoImpl”的UserDaoImpl实例。
当Service需要使用Spring创建的名字叫“userDaoImpl”的UserDaoImpl实例时，就可以使用@Resource(name = "userDao")注解告诉Spring，Spring把创建好的userDaoImpl注入给Service即可。
*/
public class UserDaoImpl implements UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertUser(User user) {
        String sql = "insert into netdisk.user (id,username,password,email,active_status,active_code,access_key,secret_key,storage_space,max_storage_number,nick_name,recover_bucket) values(?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql,user.getId(),user.getUsername(),user.getPassword(),user.getEmail(),user.getActive_status(),user.getActiveCode(),user.getAccess_key(),user.getSecret_key(),user.getStorageSpace(),user.getMaxStorageNumber(),user.getNick_name(),user.getRecoverBucket());
    }

    @Override
    public User getUserByName(String username) {
        QueryRunner queryRunner = new QueryRunner();

        String sql = "select * from netdisk.user where username = ?";
        User user = new User();
        jdbcTemplate.query(sql, new Object[]{username}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                user.setId(resultSet.getInt("id"));
                user.setUsername(username);
                user.setPassword(resultSet.getString("password"));
                user.setEmail(resultSet.getString("email"));
                user.setActiveCode(resultSet.getString("active_code"));
                user.setActive_status(resultSet.getInt("active_status"));
                user.setSecurityCode(resultSet.getInt("security_code"));
                user.setAccess_key(resultSet.getString("access_key"));
                user.setSecret_key(resultSet.getString("secret_key"));
                user.setStorageSpace(resultSet.getInt("storage_space"));
                user.setMaxStorageNumber(resultSet.getInt("max_storage_number"));
                user.setNick_name(resultSet.getString("nick_name"));
                user.setHeader_img(resultSet.getString("header_img"));
            }
        });
        return user;
    }

    @Override
    //激活后更新用户信息(code设为null state设为1表示已经激活)
    public void activeUser(User user) {
        String sql = "update netdisk.user set active_status=?,active_code=? where id=?";
        Object[] params = {user.getActive_status(), user.getActiveCode(), user.getId()};
        jdbcTemplate.update(sql,params);
    }

    @Override
    public void deleteUser(Integer id) {

    }

    @Override
    public User selectUserByActiveCode(String activeCode) {
        QueryRunner queryRunner = new QueryRunner();

        String sql = "select * from netdisk.user where active_code=?";
        User user = new User();
        jdbcTemplate.query(sql, new Object[]{activeCode}, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setEmail(resultSet.getString("email"));
                user.setActiveCode(activeCode);
                user.setActive_status(resultSet.getInt("active_status"));
            }
        });
                return user;
    }

    @Override
    public User findUserByEmail(String email) {
        QueryRunner queryRunner = new QueryRunner();

        String sql = "select * from netdisk.user where email=?";
        User user= new User();

        jdbcTemplate.query(sql, new Object[]{email}, new RowCallbackHandler(){
            @Override
            public void processRow(ResultSet resultSet) throws SQLException {

                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setEmail(email);
                user.setActiveCode(resultSet.getString("active_code"));
                user.setActive_status(resultSet.getInt("active_status"));
                user.setSecurityCode(resultSet.getInt("security_code"));
                user.setStorageSpace(resultSet.getInt("storage_space"));
                user.setMaxStorageNumber(resultSet.getInt("max_storage_number"));
            }
        });
        return user;
    }

    @Override
    public void updatePassword(User user) {
        String sql = "update netdisk.user set password=? where id=?";
        Object[] params = {user.getPassword(), user.getId()};
        jdbcTemplate.update(sql,params);
    }

    @Override
    public void updateSecurity(User user) {
        String sql = "update netdisk.user set security_code=? where id=?";
        Object[] params = {user.getSecurityCode(), user.getId()};
        jdbcTemplate.update(sql,params);
    }

    @Override
    public boolean CheckUserName(String username) {
        QueryRunner queryRunner = new QueryRunner();

        String sql = "select ifnull((select id  from netdisk.user where username=? limit 1 ), 0)";

        int count = jdbcTemplate.queryForObject(sql, new Object[]{username}, Integer.class);
        if(count!=0)
            return true;
        else
            return false;
    }

    @Override
    public List<User> getAllUser() {
        QueryRunner queryRunner = new QueryRunner();

        String sql = "select * from netdisk.user ";

        List<User> UserList = jdbcTemplate.query(sql,new MyRowMapper());
        return UserList;
    }

    @Override
    public void updateUserInfo(User user) {
        String sql = "update netdisk.user set username=? , password=?, email=? , active_status=? ,storage_space=?,max_storage_number=? where id=?";
        jdbcTemplate.update(sql,user.getUsername(),user.getPassword(),user.getEmail(),user.getActive_status(),user.getStorageSpace(),user.getMaxStorageNumber(),user.getId());
    }

    @Override
    public void deleteUser(String username) {
        String sql = "DELETE FROM netdisk.user WHERE username=?";
        jdbcTemplate.update(sql,username);
    }

    @Override
    public void setStorageSpace(User user,Integer space) {
        String sql = "update netdisk.user set storage_space=? where id=?";
        Object[] params = {space, user.getId()};
        jdbcTemplate.update(sql,params);
    }

    @Override
    public void setMaxStorageNumber(User user, Integer number) {
        String sql = "update netdisk.user set storage_space=? where id=?";
        Object[] params = {number, user.getId()};
        jdbcTemplate.update(sql,params);
    }

    @Override
    public void updateUserInfoByself(User user) {
        String sql = "update netdisk.user set username=? , password=?, email=?, header_img=?,nick_name = ? where id=?";
        jdbcTemplate.update(sql,user.getUsername(),user.getPassword(),user.getEmail(),user.getHeader_img(),user.getNick_name(),user.getId());
    }


    class MyRowMapper implements RowMapper<User>{

        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();

            //给新的User赋值，相当于取出集合中的值赋给新的User对象然后返回
            user.setId(resultSet.getInt("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setEmail(resultSet.getString("email"));
            user.setActive_status(resultSet.getInt("active_status"));
            user.setAccess_key(resultSet.getString("access_key"));
            user.setSecret_key(resultSet.getString("secret_key"));
            user.setRecoverBucket(resultSet.getString("recover_bucket"));
            return user;
        }
    }
}
