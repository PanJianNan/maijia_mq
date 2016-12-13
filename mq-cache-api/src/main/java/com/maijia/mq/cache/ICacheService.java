package com.maijia.mq.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 *
 * @author panjn
 * @date 2016/12/3
 */
public interface ICacheService {
    /**
     * Delete given {@code keys}.
     * <p/>
     * See http://redis.io/commands/del
     *
     * @param keys
     * @return The number of keys that were removed.
     */
    long del(final String... keys);

    /**
     * Set the {@code value} and expiration in {@code seconds} for {@code key}.
     * <p/>
     * See http://redis.io/commands/setex
     *
     * @param key
     * @param seconds
     * @param value
     */
    void set(final String key, final Object value, final long seconds);

    /**
     * Get the value of {@code key}.
     * <p/>
     * See http://redis.io/commands/get
     *
     * @param key
     * @return
     */
    <T> T get(final String key);

    /**
     * Find all keys matching the given {@code pattern}.
     * <p/>
     * See http://redis.io/commands/keys
     *
     * @param pattern
     * @return
     */
    Set<String> keys(final String pattern);

    /**
     * Use a cursor to iterate over keys.
     * <p/>
     * See http://redis.io/commands/scan
     *
     * @param pattern
     * @param count
     * @return
     * @since redis v2.8
     */
    List<String> scan(final String pattern, final long count);

    /**
     * Determine if given {@code key} exists.
     * <p/>
     * See http://redis.io/commands/exists
     *
     * @param key
     * @return
     */
    boolean exists(final String key);

    /**
     * Set time to live for given {@code key} in seconds.
     * <p/>
     * See http://redis.io/commands/expire
     *
     * @param key
     * @param seconds
     * @return
     */
    boolean expire(final String key, final long seconds);

    /**
     * Get the time to live for {@code key} in seconds.
     * <p/>
     * See http://redis.io/commands/ttl
     *
     * @param key
     * @return
     */
    long ttl(final String key);

    /**
     * Delete all keys of the currently selected database.
     * <p/>
     * See http://redis.io/commands/flushdb
     */
//    public void flushDB() ;

    /**
     * Get the total number of available keys in currently selected database.
     * <p/>
     * See http://redis.io/commands/dbsize
     *
     * @return
     */
    long dbSize();

    /**
     * Test connection.
     * <p/>
     * See http://redis.io/commands/ping
     *
     * @return Server response message - usually {@literal PONG}.
     */
    String ping();

    /**
     * Set the {@code value} of a hash {@code field}.
     *
     * @param key
     * @param field
     * @param value
     * @return See http://redis.io/commands/hset
     */
    boolean hSet(final String key, final String field, final Object value);

    /**
     * Get value for given {@code field} from hash at {@code key}.
     * <p/>
     * See http://redis.io/commands/hget
     *
     * @param key
     * @param field
     * @return
     */
    <T> T hGet(final String key, final String field);

    /**
     * Get key set (fields) of hash at {@code key}.
     * <p/>
     * See http://redis.io/commands/h?
     *
     * @param key
     * @return
     */
    Set<String> hKeys(final String key);

    /**
     * Delete given hash {@code fields}.
     *
     * @param key
     * @param fields
     * @return See http://redis.io/commands/hdel
     */
    long hDel(final String key, final String... fields);

    /**
     * Set multiple hash fields to multiple values using data provided in {@code hashes}
     *
     * @param key
     * @param hashes See http://redis.io/commands/hmset
     */
    void hMSet(final String key, final Map<String, Object> hashes);

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/lpush
     *
     * @param key
     * @param values
     * @return
     */
    long lPush(final String key, final Object... values);

    /**
     * Get elements between {@code begin} and {@code end} from list at {@code key}.
     * <p/>
     * See http://redis.io/commands/lrange
     *
     * @param key
     * @param begin
     * @param end
     * @return
     */
    List<Object> lRange(final String key, final long begin, final long end);

    /**
     * Removes the first {@code count} occurrences of {@code value} from the list stored at {@code key}.
     * <p/>
     * See http://redis.io/commands/lrem
     *
     * @param key
     * @param count
     * @param value
     * @return
     */
    long lRem(final String key, final long count, final Object value);

    /**
     * Trim list at {@code key} to elements between {@code begin} and {@code end}.
     * <p/>
     * See http://redis.io/commands/ltrim
     *
     * @param key
     * @param begin
     * @param end
     */
    void lTrim(final String key, final long begin, final long end);

    /**
     * Removes and returns first element in list stored at {@code key}.
     * <p/>
     * See http://redis.io/commands/lpop
     *
     * @param key
     * @return
     */
    Object lPop(final String key, final long begin, final long end);

    /**
     * Get the size of list stored at {@code key}.
     * <p/>
     * See http://redis.io/commands/llen
     *
     * @param key
     * @return
     */
    long lLen(final String key, final long begin, final long end);

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/blpop
     *
     * @param timeout
     * @param key
     * @return
     */
    Object bLPop(final int timeout, final String key);

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/brpop
     *
     * @param timeout
     * @param key
     * @return
     */
    Object bRPop(final int timeout, final String key);


}
