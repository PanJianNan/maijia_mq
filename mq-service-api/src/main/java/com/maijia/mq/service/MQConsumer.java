package com.maijia.mq.service;

import com.maijia.mq.domain.Message;

/**
 * MQConsumer
 *
 * @author panjn
 * @date 2016/10/26
 */
public interface MQConsumer {
    /**
     * 自定义消息处理
     *
     * @param message
     */
    void handleDelivery(Message message);
}
