package com.maijia.mq.service;

/**
 * 对外提供的消息队列服务接口
 *
 * @author panjn
 * @date 2016/10/26
 */
public interface IMqService {
    /**
     * 生产消息
     *
     * @param queueName 队列名称
     * @param msg       消息
     * @return
     */
    boolean produce(String queueName, Object msg);

    /**
     * 消费消息
     *
     * @param queueName 队列名称
     * @return
     */
    Object consume(String queueName);

    /**
     * 与MJMQ建立连接
     * @param host MJMQ地址
     * @return
     */
    Connection newConnection(String host);
}
