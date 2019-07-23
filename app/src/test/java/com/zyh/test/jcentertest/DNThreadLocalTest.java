package com.zyh.test.jcentertest;

import org.junit.Test;

/**
 * 描述
 *
 * @author zhangyonghui
 * 创建日期 2019-06-24
 */
public class DNThreadLocalTest {
    @Test
    public void test(){
        final ThreadLocal<String> threadLocal = new ThreadLocal<String>(){
            @Override
            protected String initialValue() {
                return "主线程默认值";
            }
        };
        System.out.println("主线程::测试threadLocal" + threadLocal.get());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String value = threadLocal.get();
                System.out.println("thread-1::默认"+value);
                threadLocal.set("thread-1::set的值");
                System.out.println("thread-1: "+threadLocal.get());
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String value = threadLocal.get();
                System.out.println("thread-2::默认"+value);
                threadLocal.set("thread-2::set的值");
                System.out.println("thread-2: "+threadLocal.get());
                threadLocal.remove();
            }
        }).start();

    }
}
