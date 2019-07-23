package com.zyh.test.jcentertest.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 描述
 *
 * @author zhangyonghui
 * 创建日期 2019-06-25
 */
public class DNMessageQueue {
    // 阻塞队列
    BlockingQueue<DNMessage> blockingQueue = new ArrayBlockingQueue<>(50);

    public void enqueueMessage(DNMessage msg) {
        try {
            blockingQueue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public DNMessage next() {
        try {
            DNMessage message = blockingQueue.take();
            return message;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
