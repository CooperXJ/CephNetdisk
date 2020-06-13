package com.cooper.demo.config;

import com.cooper.demo.service.Security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

@EnableWebSecurity
public class MySecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    UserDetailsService customUserService() { // 注册UserDetailsService 的bean
        System.out.println("UserDetailService");
        return new CustomUserDetailsService();
    }

    //实现密码的加密，在springsecurity5.0版本之后，如果不对密码进行passwordEncoder，会抛出There is no PasswordEncoder mapped for the id "null"异常
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        System.out.println("passwordEncoder");
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    //制定认证规则

//    //内存中生成的用户
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //super.configure(auth);
//
//        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder()).withUser("xuejin").password(new BCryptPasswordEncoder().encode("123456")).roles("VIP1");
//    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println("configure");
        auth.userDetailsService(customUserService()).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //super.configure(http);
        //定制请求的授权规则
        //此处为链式执行
        http.authorizeRequests().antMatchers("/UserLogin").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/user/home").hasRole("visitor")
                .antMatchers("/user/chat").hasRole("visitor")
                .antMatchers("/user/**").hasRole("VIP1")
                .antMatchers("/admin/**").hasRole("admin");//这里必须是"**"不能是"*"

        //关闭csrf()  本身与框架有冲突
        http.csrf().disable();

        //开启自动配置功能的登录功能
        http.formLogin().usernameParameter("username").passwordParameter("password").loginPage("/UserLogin").defaultSuccessUrl("/user/home");//这里相当于向浏览器发送“/UserLogin”的请求
        /*
              1.login来到登陆页面
              2.重定向到/login？error表示登录失败
              3.更多详细规定
         */

        //开启自动配置的注销功能
        http.logout().logoutSuccessUrl("/UserLogin");//注销成功以后来到首页
        //1.访问/logout表示用户注销，清空session
        //2.注销成功会返回/login？logout页面

        //开启记住我功能
        http.rememberMe().rememberMeParameter("remember");
        //1.登录成功之后将cookie发送到浏览器保存，以后访问页面带上这个Cookie，只要通过检查就可以免登录
        //点击注销会消除cookie
    }
}
