package com.maijia.mq.producer;


import com.maijia.mq.cache.ICacheService;
import com.maijia.mq.cahce.redis.util.RedisUtil;
import com.maijia.mq.domain.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * RedisProducer
 *
 * @author panjn
 * @date 2016/10/20
 */
@Service
public class RedisProducer implements Producer {

    @Resource
    private ICacheService cacheService;

    public synchronized boolean produce(String queueName, Object rawMsg) {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }
        if (rawMsg == null) {
            throw new IllegalArgumentException("消息不能为空！");
        }
//        cacheService.set("maijia_mq:temp_test", msg, 60L);
        System.out.println(RedisUtil.buildCacheKey(queueName));

        Message message = new Message(rawMsg);
        cacheService.lPush(RedisUtil.buildCacheKey(queueName), message);
//        cacheService.lPush("maijia_mq:temp_test:queue1", msg);

        return true;
    }
}
