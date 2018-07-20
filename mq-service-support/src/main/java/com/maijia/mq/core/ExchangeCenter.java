package com.maijia.mq.core;

import com.maijia.mq.client.ExchangeType;
import com.maijia.mq.producer.Producer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ExchangeCenter
 *
 * @author panjn
 * @date 2016/12/28
 */
@Component
public class ExchangeCenter {

    private final Logger logger = Logger.getLogger(this.getClass());

    public Map<String, Set<String>> exchangeMap = new HashMap<>();

    /**
     * 将消息通过交换器分发到消息队列中
     *
     * @param producer
     * @param exchangeName
     * @param exchangeType
     * @param queueName
     * @param rawMsg
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean transmit(final Producer producer, String exchangeName, ExchangeType exchangeType, String queueName, Object rawMsg) throws IOException, InterruptedException {
        switch (exchangeType) {
            case DIRECT:
                return producer.produce(queueName, rawMsg);
            case FANOUT:
                Set<String> queueSet = exchangeMap.get(exchangeName);
                if (queueSet != null) {
                    final Object targetMsg = rawMsg;
                    for (final String queue : queueSet) {
                        Thread thread = new Thread(() -> {
                            try {
                                producer.produce(queue, targetMsg);
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
                        thread.start();
                    }
                }
                break;
            case TOPIC:
                //todo topic type 暂不支持
                return false;
            default:
                return producer.produce(queueName, rawMsg);
        }
        return true;
    }
}
