package com.maijia.mq.consumer;

import com.maijia.mq.domain.MQData;
import com.maijia.mq.domain.Message;
import com.maijia.mq.domain.MessageQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * DefaultConsumer
 *
 * @author panjn
 * @date 2016/12/6
 */
@Service
public class DefaultConsumer implements Consumer {

    @Override
    public Message take(String queueName) throws InterruptedException {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }

        MessageQueue mq = MQData.QUEUE_MAP.get(queueName);
        if (mq == null) {
            mq = new MessageQueue();
            MQData.QUEUE_MAP.put(queueName, mq);
        }

        return (Message) mq.take();
    }

    /**
     * 消费消息,一旦消息为空则返回null
     *
     * @param queueName
     * @return
     */
    @Override
    public Message poll(String queueName) throws IOException, InterruptedException {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }

        MessageQueue mq = MQData.QUEUE_MAP.get(queueName);
        if (mq == null) {
            mq = new MessageQueue();
            MQData.QUEUE_MAP.put(queueName, mq);
        }

        return (Message) mq.poll();
    }
}
