package com.maijia.mq.rpc;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * 使用socket进行心跳检测的线程，发起者为服务端，
 *
 * @author panjn
 * @date 2019/1/9
 */
public class HeartBeatThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Socket socket;

    private String remoteInfo;

    public HeartBeatThread(Socket socket) {
        this.socket = socket;
        socket.getRemoteSocketAddress();
        this.setName("HeartBeatThread-" + socket.getInetAddress().getHostAddress() + "-" + socket.getPort());
        this.remoteInfo = JSONObject.toJSONString(socket.getRemoteSocketAddress());
        logger.info(String.format("初始化心跳检测：%s", remoteInfo));
    }

    @Override
    public void run() {
        //每隔5秒进行一次心跳检测
        try {
//            int i = 0;
            while (true) {
                logger.debug(String.format("进行心跳检测：%s", remoteInfo));
//                System.out.println(++i);
                socket.sendUrgentData(0xFF);
                Thread.sleep(5000L);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
