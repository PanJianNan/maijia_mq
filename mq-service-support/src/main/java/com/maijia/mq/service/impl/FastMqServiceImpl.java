package com.maijia.mq.service.impl;

import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.client.ExchangeType;
import com.maijia.mq.consumer.DefaultConsumer;
import com.maijia.mq.core.ExchangeCenter;
import com.maijia.mq.domain.Message;
import com.maijia.mq.producer.DefaultProducer;
import com.maijia.mq.service.IFastMqService;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 消息队列服务
 *
 * @author panjn
 * @date 2016/10/26
 */
@Service
public class FastMqServiceImpl implements IFastMqService {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private DefaultProducer defaultProducer;
    @Resource
    private DefaultConsumer defaultConsumer;
    @Resource
    private ExchangeCenter exchangeCenter;

    /**
     * 生产消息
     *
     * @param queueName 队列名称
     * @param rawMsg   消息
     * @return
     */
    @Override
    public boolean produce(String queueName, Object rawMsg) throws IOException, InterruptedException {
        System.out.println("有人来下蛋了" + rawMsg);
        return defaultProducer.produce(queueName, rawMsg);
    }

    /**
     * 生产消息
     *
     * @param channel 信道
     * @param rawMsg 消息
     * @return
     */
    @Override
    public boolean produce(Channel channel, Object rawMsg) throws IOException, InterruptedException {
        if (channel == null) {
            throw new NullPointerException("channel is NULL");
        }

        if (rawMsg == null) {
            throw new NullPointerException("message is NULL");
        }

        String queueName = channel.getQueueName();
        ExchangeType exchangeType = channel.getExchangeType();
        if (!ExchangeType.FANOUT.equals(exchangeType) && StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("channel's queueName must be not blank when exchangeType isn't fanout");
        }

        return exchangeCenter.transmit(defaultProducer, channel.getExchangeName(), exchangeType, queueName, rawMsg);
    }

    /**
     * 消费消息
     *
     * @param queueName 队列名称
     * @return
     */
    @Override
    public Object consume(String queueName) throws IOException, InterruptedException {
        System.out.println("有人偷蛋");
        Object obj = defaultConsumer.poll(queueName);
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
    public Connection newConnection(String host) throws IOException {
        Connection connection = new Connection();
        connection.setHost(host);

        //需求：在使用ServerSocket服务端时，需要获取得到系统的空闲端口，再将此端口注册到远端的路由上。
        //很简单，在初始化ServerSocket的时候指定其端口为0（不指定时使用默认值-1），这样就会返回系统分配的空闲端口了。
        ServerSocket serverSocket; //读取空闲的可用端口
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
        int port = serverSocket.getLocalPort();
        logger.info("系统分配的端口号 port=" + port);
        connection.setPort(port);

        MQThread mqThread = new MQThread(serverSocket);
        Thread listenThread = new Thread(mqThread);
        listenThread.start();//开启监听

        return connection;
    }

    /**
     * register a exhange binding queue
     *
     * @param exchangeName
     * @param queueName
     */
    @Override
    public void registerExchange(String exchangeName, String queueName) {
        if (StringUtils.isBlank(exchangeName)) {
            throw new IllegalArgumentException("exchangeName is blank");
        }
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName is blank");
        }
        Set<String> queueSet = exchangeCenter.exchangeMap.get(exchangeName);
        if (queueSet == null) {
            queueSet = new HashSet();
            queueSet.add(queueName);
            exchangeCenter.exchangeMap.put(exchangeName, queueSet);
        } else {
            queueSet.add(queueName);
        }
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
                    logger.error(e.getMessage(), e);
                    throw e;
                }

                //todo 心跳检测在windows中最多值允许17次，超过会发生异常，linux则不会(wtf！,linux也是17次)
                //2016年12月19日，实测，即使没有心跳检测，发现一旦客户端断开之后，服务端会先尝试Connection reset，重连失败后Socket Closed，占用的端口资源也会释放
                /*HeartBeatThread heartBeatThread = new HeartBeatThread(socket);
                Thread hbt = new Thread(heartBeatThread);
                hbt.start();
                hbt.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        try {
                            serverSocket.close();
                        } catch (IOException e1) {
                            logger.error(e1.getMessage(), e1);
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
                        Message message = defaultConsumer.take((String) recevice);
                        System.out.println("有货到，马上消费：" + message);
                        //备份信息以便消费失败时回滚该消息
                        failMsgMap.put((String) recevice, message);

                        os.writeObject(message);
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
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    for (Map.Entry<String, Object> entry : failMsgMap.entrySet()) {
                        defaultProducer.produce(entry.getKey(), entry.getValue());
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

    class HeartBeatThread implements Runnable {

        private Socket socket;

        public HeartBeatThread(Socket socket) {
            this.socket = socket;
            logger.info("初始化心跳检测");
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
                int i = 0;
                while (true) {
                    System.out.println(++i);
                    socket.sendUrgentData(0xFF);
                    Thread.sleep(5000);
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

}
