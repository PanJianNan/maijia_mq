package com.maijia.mq.consumer;

/**
 * Consumer
 *
 * @author panjn
 * @date 2016/12/6
 */
public interface Consumer {
    /**
     * 消费消息
     * @param queueName
     * @return
     */
    Object consume(String queueName);
}
