package com.maijia.mq.service.impl;

import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.client.ExchangeType;
import com.maijia.mq.core.ExchangeCenter;
import com.maijia.mq.producer.Producer;
import com.maijia.mq.rpc.MqListenThread;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

/**
 * AbstractMqService
 *
 * @author panjn
 * @date 2019/1/9
 */
public abstract class AbstractMqService {

    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * 创建ServerSocket，同时为其绑定一个消息的监听线程
     *
     * @param host
     * @param mqListenThread
     * @return
     * @throws IOException
     */
    protected Connection newConnection(String host, MqListenThread mqListenThread) throws IOException {
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

        //为ServerSocket绑定一个消息的监听线程
        mqListenThread.setServerSocket(serverSocket);
        mqListenThread.setName("MqListenThread-port:" + port);
        mqListenThread.start();//开启监听

        return connection;
    }

    protected void registerExchange(ExchangeCenter exchangeCenter, String exchangeName, String queueName) {
        if (StringUtils.isBlank(exchangeName)) {
            throw new IllegalArgumentException("exchangeName can't be blank");
        }
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName can't be blank");
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

    protected boolean produce(Channel channel, Object rawMsg, ExchangeCenter exchangeCenter, Producer producer) throws IOException, InterruptedException {
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

        return exchangeCenter.transmit(producer, channel.getExchangeName(), exchangeType, queueName, rawMsg);
    }
}
