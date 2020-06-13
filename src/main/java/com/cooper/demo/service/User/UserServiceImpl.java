package com.cooper.demo.service.User;

import com.cooper.demo.Bean.User;
import com.cooper.demo.dao.UserDaoImpl;
import com.cooper.demo.service.Mail.MailService;
import com.cooper.demo.service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDaoImpl userDaoImpl;

    @Autowired
    private MailService mailService;
    /*
        @Cacheable的运行流程：
        1.方法运行之前先去查询Cache（缓存组件），按照cacheNames指定的名字获取；
        （CacheManager先获取相应的缓存），第一次获取缓存如果没有Cache组件就会自动创建
        2.去Cache中查找缓存的内容，使用一个key，默认就是方法的参数
            key是按照某种策略生成的，某人使用的是keyGenerator生成的，默认使用SimplekeyGenerator生成key；
            SimpleKeyGenerator生成key的默认策略：
                如果没有参数key  = new SimpleKey();
                如果有一个参数：key=参数的值
                如果有多个参数：key = new SimpleKey（params）
        3.没有查到缓存就调用目标方法
        4.将目标方法返回的结果放进缓存中

        @Cacheable标注的方法执行钱先检查缓存中有没有这个数据，默认按照参数的值作为key去查询缓存
        如果没有就运行方法将结果放入到混村中，以后再来调用可以直接使用缓存中的数据

        核心：
            1）使用CacheManager按照名字得到Cache
            2）key使用keyGenerator生成，默认是SimpleKeyGenerator
    */

    //@Cacheable的参数列表
    /*
        1.condition 如何判断为true则启用缓存
        2.unless 如果判断为true则不启用缓存
    */

//    @Cacheable(cacheNames = "user")
////    public User getUser(Integer id){
////        System.out.println("查询"+id+"号员工");
////        User user = userMapper.getUserByID(id);
////        return user;
////    }

    /*
        1.@CachePut:既调用了方法，又更新缓存了数据
        运行时机：
        1.先调用目标方法
        2.将目标方法的结果缓存起来

        注意点：key需要改成#result.id
        这样的话查询与更新之后的缓存的key就一样了
        因为使用CachePut的时候如果不指定key的话，会自动将缓存的key命名为返回的对象，
        这样的话与原来查询id中的key不一致，因此查询的时候还是会使用以前的缓存的，而CachePut
        所产生的缓存因为名字不同，因此无法调用
     */

//    @CachePut(cacheNames = "user",key = "#result.id")
//    public User updateUser(User user)
//    {
//        System.out.println("updateUser"+user);
//        userMapper.updateUser(user);
//        return user;
//    }

    //清空缓存
    /*
        1.@CacheEvict
        key:指定要删除的缓存中的指定数据
        allEntries = true;指定清楚这个缓存中的数据
        beforeInvocation = false ;缓存的清除在方法之前
        默认是缓存的清楚再方法执行之后，如果出现异常的话则缓存不会被清楚
        但是设置成true的话，就会使得缓存会在方法被执行之前清除，即使后面出现错误的话
    */

//    @CacheEvict(cacheNames = "user",key = "#id")
//    public void deleteUser(Integer id)
//    {
//        System.out.println("deleteUser"+id);
//        userMapper.deleteUser(id);
//    }
//
//    @CachePut(cacheNames = "user",key = "#result.id")
//    public void addUser(User user)
//    {
//        System.out.println("addUser"+user);
//        userMapper.insertUser(user);
//    }




    @Override
    public void add(User user,boolean flag) {
        userDaoImpl.insertUser(user);
        //判断是否是通过admin创建的用户
        if(flag)
        {
            //获取激活码
            String code = user.getActiveCode();
            System.out.println("激活码"+code);
            //主题
            String subject = "来自Cooper的激活邮件";
            //上面的激活码发送到用户的注册邮箱
            String context = "<a href=\"http://localhost:8080/register/checkCode?code="+code+"\">激活请点击:"+code+"</a>";
            //发送激活邮件
            mailService.sendMimeMail(user.getEmail(),subject,context);
        }
    }

    @Override
    public User getUserByActiveCode(String activeCode) {
        return userDaoImpl.selectUserByActiveCode(activeCode);
    }

    @Override
    public void activeUser(User user) {
        userDaoImpl.activeUser(user);
    }


    @Override
    public User get(User user) {
        return user;
    }

    @Override
    public User findUserByMail(String email) throws Exception {
        return userDaoImpl.findUserByEmail(email);
    }

    @Override
    public void updatePassword(User user) {
        userDaoImpl.updatePassword(user);
    }

    @Override
    public void updateSecurityCode(User user){
        userDaoImpl.updateSecurity(user);
    }

    @Override
    public User getUserByName(String username) {
        return userDaoImpl.getUserByName(username);
    }

    @Override
    public List<User> getAllUser() {
        return userDaoImpl.getAllUser();
    }

    @Override
    public void updateUserInfo(User user) {
        userDaoImpl.updateUserInfo(user);
    }

    @Override
    public void deleteUser(String username) {
        userDaoImpl.deleteUser(username);
    }

    @Override
    public void setStorageSpace(User user,Integer space) {
        userDaoImpl.setStorageSpace(user,space);
    }

    @Override
    public void updateUserInfoByself(User user) {
        userDaoImpl.updateUserInfoByself(user);
    }


    public void SendSecurityCode(String mail,Integer code) throws Exception {
        //获取验证码
        String codes=code.toString();
        System.out.println("验证码"+code);
        //主题
        String subject = "来自Cooper的重置密码邮件";
        //上面的激活码发送到用户的注册邮箱
        String context = "<h1>验证码</h1><h2>" +    code + "</h2>";
        //发送激活邮件
        mailService.sendMimeMail(mail,subject,context);
    }

    public boolean checkUserName(String username)
    {
        return userDaoImpl.CheckUserName(username);
    }
}
