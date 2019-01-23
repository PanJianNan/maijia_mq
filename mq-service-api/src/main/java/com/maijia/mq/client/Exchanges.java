package com.maijia.mq.client;

import com.maijia.mq.rpc.RpcFramework;
import com.maijia.mq.service.IExchangeService;
import com.maijia.mq.util.ConstantUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Exchanges
 *
 * @author panjn
 * @date 2016/12/28
 */
public class Exchanges {

    private static boolean init = false;

    /**
     * key-exchangeName, value-queueNameSet
     */
    public static Map<String, Set<String>> exchangeMap = new HashMap<>();

    static {
        init(MqConfig.host, ConstantUtils.NIO_RPC_PORT);
    }

    public static boolean contains(String exchangeName, String queueName) {
        Set<String> queueNameSet = exchangeMap.get(exchangeName);
        if (queueName == null) {
            return false;
        }
        return queueNameSet.contains(queueName);
    }

    public static void put(String exchangeName, String queueName) {
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
    }

    public static void init(String host, int port) {
        if (init) {
            return;
        }
        IExchangeService exchangeService = RpcFramework.refer(IExchangeService.class, host, port, RpcFramework.DEFAULT_VERSION);

        Map<String, Set<String>> serverExchangeMap = exchangeService.getExchangeMap();
        exchangeMap = serverExchangeMap;

        init = true;
    }


}
