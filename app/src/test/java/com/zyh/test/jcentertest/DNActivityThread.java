package com.zyh.test.jcentertest;

import com.zyh.test.jcentertest.core.DNHandler;
import com.zyh.test.jcentertest.core.DNLooper;
import com.zyh.test.jcentertest.core.DNMessage;

import org.junit.Test;

/**
 * 描述
 *
 * @author zhangyonghui
 * 创建日期 2019-06-25
 */
public class DNActivityThread {
    @Test
    public void main() {
        /*DNLooper.prepare();
        // 创建handler对象
        final DNHandler handler = new DNHandler() {
            @Override
            public void handlerMessage(DNMessage msg) {
                switch (msg.what) {
                    case 1:
                        System.out.println(msg.obj.toString());
                        break;
                    default:
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                DNMessage msg = new DNMessage();
                msg.obj = "传输内容:test";
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                DNMessage msg = new DNMessage();
                msg.obj = "传输内容:test2";
                msg.what = 1;
                handler.sendMessage(msg);
            }
        }).start();
        DNLooper.loop();*/
    }
}
