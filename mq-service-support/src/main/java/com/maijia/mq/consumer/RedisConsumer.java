package com.maijia.mq.consumer;

import com.maijia.mq.cache.ICacheService;
import com.maijia.mq.cahce.redis.util.RedisUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * RedisConsumer
 *
 * @author panjn
 * @date 2016/12/6
 */
@Service
public class RedisConsumer implements Consumer {

    @Resource
    ICacheService cacheService;

    public Object consume(String queueName) {
        return cacheService.bRPop(0, RedisUtil.buildCacheKey(queueName));
    }
}
