package com.maijia.mq.cahce.redis;

import com.maijia.mq.cache.ICacheService;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.SerializationUtils;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * 缓存--Redis实现
 *
 * @author panjn
 * @date 2016/12/3
 */
@Component
public class RedisCacheImpl implements ICacheService {

    private final Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    private static final StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    private static final JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();

    @Override
    public long del(final String... keys) {
        return (long) redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] bytes = new byte[keys.length][];
                int index = 0;
                for (String key : keys) {
                    bytes[index++] = rawKey(key);
                }
                return connection.del(bytes);
            }
        });
    }

    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.setEx(key, liveTime, value);
                return null;
            }
        });
    }

    @Override
    public void set(String key, Object value, long liveTime) {
        this.set(rawKey(key), rawValue(value), liveTime);
    }

    public void set(String key, Object value) {
        this.set(key, value, 0L);
    }

    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    @Override
    public <T> T get(final String key) {
        return redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] bs = connection.get(rawKey(key));
                return (T) deserializeValue(bs);
            }
        });
    }

    @Override
    public Set<String> keys(final String pattern) {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                Set<byte[]> rawKeys = connection.keys(rawKey(pattern));
                return SerializationUtils.deserialize(rawKeys, stringRedisSerializer);
            }
        });
    }

    @Override
    public List<String> scan(final String pattern, final long count) {
        return redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> rawKeys = new ArrayList<byte[]>();
                Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(pattern).count(count).build());
                while (cursor.hasNext()) {
                    rawKeys.add(cursor.next());
                }
                try {
                    cursor.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                return SerializationUtils.deserialize(rawKeys, stringRedisSerializer);
            }
        });
    }

    @Override
    public boolean exists(final String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.exists(rawKey(key));
            }
        });
    }

    @Override
    public boolean expire(final String key, final long liveTime) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.expire(rawKey(key), liveTime);
            }
        });
    }

    @Override
    public long ttl(final String key) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.ttl(rawKey(key));
            }
        });
    }

//    @Override
//    public void flushDB(){
//        redisTemplate.execute(new RedisCallback<Object>() {
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                connection.flushDb();
//                return null;
//            }
//        });
//    }

    @Override
    public long dbSize() {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
    }

    @Override
    public String ping() {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.ping();
            }
        });
    }

    @Override
    public boolean hSet(final String key, final String field, final Object value) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.hSet(rawKey(key), rawKey(field), rawValue(value));
            }
        });
    }

    @Override
    public <T> T hGet(final String key, final String field) {
        return redisTemplate.execute(new RedisCallback<T>() {
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                return (T) deserializeValue(connection.hGet(rawKey(key), rawKey(field)));
            }
        });
    }

    @Override
    public Set<String> hKeys(final String key) {
        return redisTemplate.execute(new RedisCallback<Set<String>>() {
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                return SerializationUtils.deserialize(connection.hKeys(rawKey(key)), stringRedisSerializer);
            }
        });
    }

    @Override
    public long hDel(final String key, final String... fields) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] bytes = new byte[fields.length][];
                int index = 0;
                for (String field : fields) {
                    bytes[index++] = rawKey(field);
                }
                return connection.hDel(rawKey(key), bytes);
            }
        });
    }

    @Override
    public void hMSet(final String key, final Map<String, Object> hashes) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Map<byte[], byte[]> newHashes = new HashMap<byte[], byte[]>(hashes.size());
                for (Map.Entry<String, Object> entry : hashes.entrySet()) {
                    newHashes.put(rawKey(entry.getKey()), rawValue(entry.getValue()));
                }
                connection.hMSet(rawKey(key), newHashes);
                return null;
            }
        });
    }

    @Override
    public long lPush(final String key, final Object... values) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] bytes = new byte[values.length][];
                int index = 0;
                for (Object value : values) {
                    bytes[index++] = rawValue(value);
                }
                return connection.lPush(rawKey(key), bytes);
            }
        });
    }

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/rpush
     *
     * @param key
     * @param values
     * @return
     */
    @Override
    public long rPush(final String key, final Object... values) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                byte[][] bytes = new byte[values.length][];
                int index = 0;
                for (Object value : values) {
                    bytes[index++] = rawValue(value);
                }
                return connection.rPush(rawKey(key), bytes);
            }
        });
    }

    @Override
    public List<Object> lRange(final String key, final long begin, final long end) {
        return redisTemplate.execute(new RedisCallback<List<Object>>() {
            public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> rawValues = connection.lRange(rawKey(key), begin, end);
                return SerializationUtils.deserialize(rawValues, jdkSerializationRedisSerializer);
            }
        });
    }

    @Override
    public long lRem(final String key, final long count, final Object value) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lRem(rawKey(key), count, rawValue(value));
            }
        });
    }

    @Override
    public void lTrim(final String key, final long begin, final long end) {
        redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.lTrim(rawKey(key), begin, end);
                return null;
            }
        });
    }

    @Override
    public Object lPop(final String key) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return deserializeValue(connection.lPop(rawKey(key)));
            }
        });
    }

    /**
     * Removes and returns first element in list stored at {@code key}.
     * <p/>
     * See http://redis.io/commands/rpop
     *
     * @param key
     */
    @Override
    public Object rPop(final String key) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return deserializeValue(connection.rPop(rawKey(key)));
            }
        });
    }

    @Override
    public long lLen(final String key, final long begin, final long end) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lLen(rawKey(key));
            }
        });
    }

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/blpop
     *
     * @param key
     * @return
     */
    @Override
    public Object bLPop(final int timeout, final String key) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> list = connection.bLPop(timeout, rawKey(key));
                return deserializeValue(list.get(0));
            }
        });
    }

    /**
     * Prepend {@code values} to {@code key}.
     * <p/>
     * See http://redis.io/commands/brpop
     *
     * @param key
     * @return
     */
    @Override
    public Object bRPop(final int timeout, final String key) {
        return redisTemplate.execute(new RedisCallback<Object>() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                List<byte[]> list = connection.bRPop(timeout, rawKey(key));
                return deserializeValue(list.get(1));
            }
        });
    }

    private byte[] rawKey(String key) {
        return stringRedisSerializer.serialize(key);
    }

    private byte[] rawValue(Object value) {
        return jdkSerializationRedisSerializer.serialize(value);
    }

    private String deserializeKey(byte[] key) {
        return stringRedisSerializer.deserialize(key);
    }

    private Object deserializeValue(byte[] value) {
        return jdkSerializationRedisSerializer.deserialize(value);
    }

    private RedisCacheImpl() {

    }
}
