package com.maijia.mq.consumer;

import com.maijia.mq.cache.ICacheService;
import com.maijia.mq.cahce.redis.util.RedisUtil;
import com.maijia.mq.domain.Message;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

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

    @Override
    public Message take(String queueName) {
        return (Message) cacheService.bRPop(0, RedisUtil.buildCacheKey(queueName));
    }

    /**
     * 消费消息,一旦消息队列为空则返回null
     *
     * @param queueName
     * @return
     */
    @Override
    public Message poll(String queueName) throws IOException, InterruptedException {
        return (Message) cacheService.rPop(RedisUtil.buildCacheKey(queueName));
    }
}
