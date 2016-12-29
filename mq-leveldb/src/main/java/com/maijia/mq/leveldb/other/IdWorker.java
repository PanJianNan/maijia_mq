package com.maijia.mq.leveldb.other;

import org.apache.log4j.Logger;

/**
 * 系统编码生成器
 *
 * @author panjn
 * @date 2016/12/16
 */
public class IdWorker {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final long workerId;
    private final static long twepoch = 1421976968066L;
    private long sequence = 0L;
    private final static long workerIdBits = 2L;
    public final static long maxWorkerId = -1L ^ -1L << workerIdBits;
    private final static long sequenceBits = 9L;

    private final static long workerIdShift = sequenceBits;
    private final static long timestampLeftShift = sequenceBits + workerIdBits;
    public final static long sequenceMask = -1L ^ -1L << sequenceBits;

    private long lastTimestamp = -1L;

    public IdWorker(final long workerId) {
        super();
        if (workerId > IdWorker.maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                    "worker Id can't be greater than %d or less than 0",
                    IdWorker.maxWorkerId));
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = this.timeGen();
        if (this.lastTimestamp == timestamp) {
            this.sequence = (this.sequence + 1) & IdWorker.sequenceMask;
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);
            }
        } else {
            this.sequence = 0;
        }
        if (timestamp < this.lastTimestamp) {
            try {
                throw new Exception(
                        String.format(
                                "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                                this.lastTimestamp - timestamp));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        this.lastTimestamp = timestamp;
        long nextId = ((timestamp - twepoch << timestampLeftShift))
                | (this.workerId << IdWorker.workerIdShift) | (this.sequence);
        return nextId;
    }

    private long tilNextMillis(final long lastTimestamp) {
        long timestamp = this.timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = this.timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}
