package com.maijia.mq.service.impl;

import com.maijia.mq.client.MqChannel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.consumer.RedisConsumer;
import com.maijia.mq.core.ExchangeCenter;
import com.maijia.mq.producer.RedisProducer;
import com.maijia.mq.rpc.MqListenThread;
import com.maijia.mq.service.ICacheMqService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 消息队列服务
 * <p>
 * 基于Redis
 *
 * @author panjn
 * @date 2016/10/26
 */
@Service
public class CacheMqServiceImpl extends AbstractMqService implements ICacheMqService {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private RedisProducer redisProducer;
    @Resource
    private RedisConsumer redisConsumer;
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
        return redisProducer.produce(queueName, rawMsg);
    }

    /**
     * 生产消息
     *
     * @param channel 信道
     * @param rawMsg 消息
     * @return
     */
    @Override
    public boolean produce(MqChannel channel, Object rawMsg) throws IOException, InterruptedException {
        return this.produce(channel, rawMsg, exchangeCenter, redisProducer);
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
        Object obj = redisConsumer.poll(queueName);
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
        MqListenThread mqListenThread = new MqListenThread(redisConsumer, redisProducer);
        return this.newConnection(host, mqListenThread);
    }

    /**
     * register a exhange binding queue
     *
     * @param exchangeName
     * @param queueName
     */
    @Override
    public void registerExchange(String exchangeName, String queueName) {
        this.registerExchange(exchangeCenter, exchangeName, queueName);
    }


}
