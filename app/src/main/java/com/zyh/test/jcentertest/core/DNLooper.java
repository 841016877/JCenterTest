package com.zyh.test.jcentertest.core;

/**
 * 描述
 *
 * @author zhangyonghui
 * 创建日期 2019-06-25
 */
public class DNLooper {
    static final ThreadLocal<DNLooper> sThreadLocal = new ThreadLocal();
    public DNMessageQueue mQueue;

    public DNLooper(){
        mQueue = new DNMessageQueue();
    }

    public static void prepare() {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("only one Looper may be created per thread");
        }
        sThreadLocal.set(new DNLooper());
    }

    public static DNLooper myLooper(){
        return sThreadLocal.get();
    }

    public static void loop() {
        // 从全局ThreadLocalMap中获取唯一,looper对象
        DNLooper dnLooper = myLooper();
        DNMessageQueue mQueue = dnLooper.mQueue;
        while (true) {
            DNMessage message = mQueue.next();
            if (message != null) {
                message.target.dispatchMessage(message);
            }
        }
    }
}
