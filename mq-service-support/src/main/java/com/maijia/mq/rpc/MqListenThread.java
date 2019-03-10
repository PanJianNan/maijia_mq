package com.maijia.mq.rpc;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.domain.Message;
import com.maijia.mq.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 监听客户端消息请求的线程
 *
 * @author panjn
 * @date 2019/1/9
 */
public class MqListenThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ServerSocket serverSocket;

    private Consumer consumer;

    private Producer producer;


    public MqListenThread(Consumer consumer, Producer producer) {
        this.consumer = consumer;
        this.producer = producer;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        if (serverSocket == null) {
            throw new RuntimeException("serverSocket can't be null");
        }

        Map<String, Object> failMsgMap = new HashMap<>();
        ObjectInputStream is = null;
        ObjectOutputStream os = null;
        Socket socket = null;
        try {
            try {
                //使用accept()阻塞等待客户端请求，有客户端
                //请求到来则产生一个Socket对象，并继续执行
                socket = serverSocket.accept();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            }

            //todo 心跳检测在windows中最多值允许17次，超过会发生异常，linux则不会(wtf！,linux也是17次)
            //2016年12月19日，实测，即使没有心跳检测，发现一旦客户端断开之后，服务端会先尝试Connection reset，重连失败后Socket Closed，占用的端口资源也会释放
            /*HeartBeatThread hbt = new HeartBeatThread(socket);
            hbt.setUncaughtExceptionHandler((t, e) -> {
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            });
            hbt.start();*/

            //由系统标准输入设备构造BufferedReader对象
            os = new ObjectOutputStream(socket.getOutputStream());
            //由Socket对象得到输出流，并构造PrintWriter对象
            is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            while (true) {
                //接收消费请求 （1.读）
                Object recevice = is.readObject();
//                    System.out.println("收到请求");

                if (!(recevice instanceof String)) {
                    os.writeObject(new IllegalArgumentException("队列名必须是字符串"));
                    continue;
                }

                //返回消息
                Message message = consumer.take((String) recevice);
//                        System.out.println("有货到，马上消费：" + message);
                //备份信息以便消费失败时回滚该消息
                failMsgMap.put((String) recevice, message);

                os.writeObject(message);
                //向客户端输出该字符串 （2.写）
                os.flush();

                //确认消费成功 （3.读）
                Object ack = is.readObject();
                if ("success".equals(ack)) {
                    logger.info("客户端消费成功 msg:" + JSONObject.toJSONString(message));
                    failMsgMap.remove(recevice);
                }

            }

        } catch (Exception e) {
            logger.info("发生异常，可能是客户端断开连接");
            logger.error(e.getMessage(), e);
        } finally {
            try {
                //todo 暂时将需要重发的消息重新存储起来
                for (Map.Entry<String, Object> entry : failMsgMap.entrySet()) {
                    producer.produce(entry.getKey(), entry.getValue());
                }

                os.close(); //关闭Socket输出流
                is.close(); //关闭Socket输入流
                socket.close(); //关闭Socket
                serverSocket.close(); //关闭ServerSocket
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
