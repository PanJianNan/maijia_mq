package com.maijia.mq.core;

import com.maijia.mq.client.ExchangeType;
import com.maijia.mq.leveldb.LevelDBPersistenceAdapter;
import com.maijia.mq.producer.Producer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final String LEVEL_DB_EXCHANGES_KEY = "system:mjmq-server:exchange-map";

    @Resource
    private LevelDBPersistenceAdapter levelDBPersistenceAdapter;

    private Map<String, Set<String>> exchangeMap = new HashMap<>();

    public Map<String, Set<String>> getExchangeMap() {
        return exchangeMap;
    }

    @PostConstruct
    public void init() {
        //暂时先存LevelDB todo 实现起来有点丑陋
        try {
            Method openMethod = LevelDBPersistenceAdapter.class.getDeclaredMethod("open");
            openMethod.setAccessible(true);
            openMethod.invoke(levelDBPersistenceAdapter);//todo 如果需要修改打开LevelDB的参数，这里将会是一个bug

            Method getMethod = LevelDBPersistenceAdapter.class.getDeclaredMethod("get", String.class, Class.class);
            getMethod.setAccessible(true);
            Object result =  getMethod.invoke(levelDBPersistenceAdapter, LEVEL_DB_EXCHANGES_KEY, Map.class);
            if (result != null) {
                exchangeMap = (Map<String, Set<String>>) result;
            }
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void registeExchange(String exchangeName, String queueName) {
        if (StringUtils.isBlank(exchangeName)) {
            throw new IllegalArgumentException("exchangeName can't be blank");
        }
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName can't be blank");
        }
        Set<String> queueSet = exchangeMap.get(exchangeName);
        if (queueSet == null) {
            queueSet = new HashSet();
            queueSet.add(queueName);
            exchangeMap.put(exchangeName, queueSet);
        } else {
            queueSet.add(queueName);
        }

        //暂时先存LevelDB
        try {
            Method putMethod = LevelDBPersistenceAdapter.class.getDeclaredMethod("put", String.class, Serializable.class);
            putMethod.setAccessible(true);
            putMethod.invoke(levelDBPersistenceAdapter, LEVEL_DB_EXCHANGES_KEY, exchangeMap);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

    }

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
                    for (String queue : queueSet) {
//                        Thread thread = new Thread(() -> {
                            try {
                                producer.produce(queue, targetMsg);
                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                            } catch (InterruptedException e) {
                                logger.error(e.getMessage(), e);
                            }
//                        });
//                        thread.start();
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
