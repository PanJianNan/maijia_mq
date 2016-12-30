package com.maijia.mq.service;

import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;

import java.io.IOException;

/**
 * 对外提供的消息队列服务接口（默认模式/文件模式）
 * 优点：占用内存最小，消息落体不丢失
 * 缺点：速度最慢，暂不支持分布式
 *
 * @author panjn
 * @date 2016/10/26
 */
public interface IMqService {
    /**
     * 生产消息
     *
     * @param queueName 队列名称
     * @param rawMsg    消息
     * @return
     */
    boolean produce(String queueName, Object rawMsg) throws IOException, InterruptedException;

    /**
     * 生产消息
     *
     * @param channel 信道
     * @param rawMsg  消息
     * @return
     */
    boolean produce(Channel channel, Object rawMsg) throws IOException, InterruptedException;

    /**
     * 消费消息
     *
     * @param queueName 队列名称
     * @return
     */
    Object consume(String queueName) throws IOException, InterruptedException;

    /**
     * 与MJMQ建立连接
     *
     * @param host MJMQ地址
     * @return
     */
    Connection newConnection(String host) throws IOException;

    /**
     * register a exhange binding queue
     *
     * @param exchangeName
     * @param queueName
     */
    void registerExchange(String exchangeName, String queueName);
}
