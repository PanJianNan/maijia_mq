package com.maijia.mq.producer;

import java.io.IOException;

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
     * @param rawMsg
     * @return
     */
    boolean produce(String queueName, Object rawMsg) throws IOException, InterruptedException;
}
