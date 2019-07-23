package com.zyh.test.jcentertest.core;

/**
 * 描述
 *
 * @author zhangyonghui
 * 创建日期 2019-06-25
 */
public class DNHandler {
    private DNLooper mDnlooper;
    private DNMessageQueue mMessageQueue;

    public DNHandler(){
        mDnlooper = DNLooper.myLooper();
        mMessageQueue = mDnlooper.mQueue;
    }

    public void handlerMessage(DNMessage msg){
    }

    public void sendMessage(DNMessage msg) {
        enqueueMessage(msg);
    }

    // 放入队列
    private void enqueueMessage(DNMessage msg) {
        msg.target = this;
        // 使用mMessageQueue将消息传入
        mMessageQueue.enqueueMessage(msg);
    }

    public void dispatchMessage(DNMessage message) {
        handlerMessage(message);
    }
}
