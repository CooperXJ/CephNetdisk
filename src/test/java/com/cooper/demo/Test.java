package com.cooper.demo;

/*
 * 终止线程的两种方式
 * 1.线程正常执行完毕(因为它自身是有次数的限制的，执行完了就结束了)
 * 2.外部干涉(加入标识)
 * 不要使用stop，destroy因为它们过时了，有点不安全
 *
 */

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class test implements Runnable{
    //1.加入标识标记线程体是否可以运行
    private boolean flag=true;  //这个就是一个开关
    private String name;

    public test(String name) {
        this.name = name;
    }
    @Override
    public void run() {
        int i=0;
        //2.关联标识为真线程体可以运行，假的时候就结束运行
        while(flag) {
            System.out.println(name+"-->"+i++);
        }
    }
    //3.对外提供方法改变标识
    public void change() {
        this.flag=false;
    }
    public void wake()
    {
        this.flag = true;
    }

    public static void main(String[] args) {
        test tt=new test("老大");
        new Thread(tt).start();

        for(int i=0;i<1000;i++) {
            if(i==50) {
                tt.change();
            }
            if(i==80)
            {
                System.out.println("come");
                tt.wake();
            }

            System.out.println("主线程在运行"  +i);
        }
    }

}