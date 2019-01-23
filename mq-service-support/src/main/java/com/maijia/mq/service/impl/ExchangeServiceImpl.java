package com.maijia.mq.service.impl;

import com.maijia.mq.core.ExchangeCenter;
import com.maijia.mq.service.IExchangeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

/**
 * ExchangeServiceImpl
 *
 * @author panjn
 * @date 2019/1/23
 */
@Service
public class ExchangeServiceImpl implements IExchangeService {

    @Resource
    private ExchangeCenter exchangeCenter;

    @Override
    public Map<String, Set<String>> getExchangeMap() {
        return exchangeCenter.getExchangeMap();
    }

    @Override
    public void registerExchange(String exchangeName, String queueName) {
        exchangeCenter.registeExchange(exchangeName, queueName);
    }
}
