package com.maijia.mq.consumer;

import com.maijia.mq.domain.MQData;
import com.maijia.mq.domain.MessageQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * DefaultConsumer
 *
 * @author panjn
 * @date 2016/12/6
 */
@Service
public class DefaultConsumer implements Consumer {
    public Object consume(String queueName) {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }

        MessageQueue mq = MQData.queueMap.get(queueName);
        if (mq == null) {
//            mq = new MessageQueue();
//            MQData.queueMap.put(queueName, mq);
            return null;
        }

        try {
            return mq.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
