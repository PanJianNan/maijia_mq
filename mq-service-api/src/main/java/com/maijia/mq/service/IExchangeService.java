package com.maijia.mq.service;

import java.util.Map;
import java.util.Set;

/**
 * IExchangeService
 *
 * @author panjn
 * @date 2019/1/23
 */
public interface IExchangeService {

    Map<String, Set<String>> getExchangeMap();

    /**
     * exchangeCenter
     * @param exchangeName
     * @param queueName
     */
    void registerExchange(String exchangeName, String queueName);
}
