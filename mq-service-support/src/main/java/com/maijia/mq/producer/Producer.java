package com.maijia.mq.producer;

/**
 * Producer
 *
 * @author panjn
 * @date 2016/12/6
 */
public interface Producer {
    /**
     * 生产消息
     * @param queueName
     * @param msg
     * @return
     */
    boolean produce(String queueName, Object msg);
}
