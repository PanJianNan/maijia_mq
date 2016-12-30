package com.maijia.mq.consumer;

import com.maijia.mq.domain.Message;

import java.io.IOException;

/**
 * Consumer
 *
 * @author panjn
 * @date 2016/12/6
 */
public interface Consumer {
    /**
     * 消费消息，当消息队列为空，则会阻塞等待直到有新的消息进入队列
     *
     * @param queueName
     * @return
     */
    Message take(String queueName) throws IOException, InterruptedException;

    /**
     * 消费消息,当消息队列为空，则返回null
     *
     * @param queueName
     * @return
     */
    Message poll(String queueName) throws IOException, InterruptedException;
}
