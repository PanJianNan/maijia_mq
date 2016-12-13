package com.maijia.mq.producer;


import com.maijia.mq.domain.MQData;
import com.maijia.mq.domain.MessageQueue;
import com.maijia.mq.producer.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * DefaultProducer
 *
 * @author panjn
 * @date 2016/10/20
 */
@Service
public class DefaultProducer implements Producer {

    public synchronized boolean produce(String queueName, Object msg) {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }
        if (msg == null) {
            throw new IllegalArgumentException("消息不能为空！");
        }

        MessageQueue mq = MQData.queueMap.get(queueName);
        if (mq == null) {
//            synchronized (MQData.queueMap) {
                mq = new MessageQueue();
                mq.add(msg);
                MQData.queueMap.put(queueName, mq);
//            }
        } else {
            mq.add(msg);
        }

        return true;
    }
}
