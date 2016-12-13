package com.maijia.mq.cahce.redis.util;

/**
 * RedisUtil
 *
 * @author panjn
 * @date 2016/12/6
 */
public class RedisUtil {

    public static final String cacheKeyPre = "maijia_mq:message:";

    /**
     * 根据消息队列名构建redis key
     *
     * @param queueName
     * @return
     */
    public static String buildCacheKey(String queueName) {
        return cacheKeyPre + queueName;
    }
}
