package com.maijia.mq.service.impl;

import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.producer.Producer;
import com.maijia.mq.service.Connection;
import com.maijia.mq.service.IMqService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息队列服务
 *
 * @author panjn
 * @date 2016/10/26
 */
@Service
public class MqServiceImpl implements IMqService {

    private static final Logger LOGGER = Logger.getLogger(MqServiceImpl.class);

    @Resource
    private Producer redisProducer;
    @Resource
    private Consumer redisConsumer;

    /**
     * 生产消息
     *
     * @param queueName 队列名称
     * @param msg       消息
     * @return
     */
    @Override
    public boolean produce(String queueName, Object msg) {
        System.out.println("有人来下蛋了");
        return redisProducer.produce(queueName, msg);
    }

    /**
     * 消费消息
     *
     * @param queueName 队列名称
     * @return
     */
    @Override
    public Object consume(String queueName) {
        System.out.println("有人偷蛋");
        Object obj = redisConsumer.consume(queueName);
        System.out.println(obj);
        return obj;
    }

    /**
     * 与MJMQ建立连接
     *
     * @param host MJMQ地址
     * @return
     */
    @Override
    public Connection newConnection(String host) {
        Connection connection = new Connection();
        connection.setHost(host);

//        需求：在使用ServerSocket服务端时，需要获取得到系统的空闲端口，再将此端口注册到远端的路由上。
//        很简单，在初始化ServerSocket的时候指定其端口为0（不指定时使用默认值-1），这样就会返回系统分配的空闲端口了。
        ServerSocket serverSocket = null; //读取空闲的可用端口
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        int port = serverSocket.getLocalPort();
        LOGGER.info("系统分配的端口号 port=" + port);
        //System.out.println("系统分配的端口号 port="+port);
        connection.setPort(port);

        MQThread mqThread = new MQThread(serverSocket);
        Thread listenThread = new Thread(mqThread);
        listenThread.start();//开启监听

        return connection;
    }


    class MQThread implements Runnable {

        private ServerSocket serverSocket;

        public MQThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }



        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
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
                    LOGGER.error(e.getMessage(), e);
                }

                //todo 心跳检测在windows中最多值允许17次，超过或发生一次，linux则不会
                /*HeartBeatThread heartBeatThread = new HeartBeatThread(socket);
                Thread hbt = new Thread(heartBeatThread);
                hbt.start();
                hbt.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        try {
                            serverSocket.close();
                        } catch (IOException e1) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                });*/

                //由系统标准输入设备构造BufferedReader对象
                os = new ObjectOutputStream(socket.getOutputStream());
                //由Socket对象得到输出流，并构造PrintWriter对象
                is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                while (true) {
                    //接收消费请求 （读）
                    Object recevice = is.readObject();
                    System.out.println("收到请求");

                    if (recevice instanceof String && !"success".equals(recevice)) {
                        //返回消息
                        Object msg = redisConsumer.consume((String) recevice);
                        System.out.println("有货到，马上消费：" + msg);
                        //备份信息以便消费失败时回滚该消息
                        failMsgMap.put((String) recevice, msg);

                        os.writeObject(msg);
                        //向客户端输出该字符串 （写）
                        os.flush();
                    }

                    //确认消费成功 （读）
                    Object ack = is.readObject();
                    if (recevice instanceof String && "success".equals(ack)) {
                        System.out.println("客户端消费成功");
                        failMsgMap.remove((String) recevice);
                    }

                    Thread.sleep(3000);
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                try {
                    for (Map.Entry<String, Object> entry: failMsgMap.entrySet()) {
                        redisProducer.produce(entry.getKey(), entry.getValue());
                    }

                    os.close(); //关闭Socket输出流
                    is.close(); //关闭Socket输入流
                    socket.close(); //关闭Socket
                    serverSocket.close(); //关闭ServerSocket
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    class HeartBeatThread implements Runnable {

        private Socket socket;

        public HeartBeatThread(Socket socket) {
            this.socket = socket;
            LOGGER.info("初始化心跳检测");
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            //每隔5秒进行一次心跳检测
            try {
                int i=0;
                while (true) {
                    System.out.println(++i);
                    socket.sendUrgentData(0xFF);
                    Thread.sleep(5000);
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                try {
                    socket.getInputStream().close();
                    socket.getOutputStream().close();
                    socket.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
