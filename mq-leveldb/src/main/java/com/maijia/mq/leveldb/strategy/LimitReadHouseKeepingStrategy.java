package com.maijia.mq.leveldb.strategy;

import com.maijia.mq.leveldb.AbstractHouseKeepingStrategy;
import com.maijia.mq.leveldb.PersistenceException;
import com.maijia.mq.leveldb.QueueMiddleComponent;

import java.io.IOException;

/**
 * 当pop了一定数量的数据之后，开始执行计划。需要设置limit read size
 *
 * @author panjn
 * @date 2016/12/16
 */
public class LimitReadHouseKeepingStrategy extends AbstractHouseKeepingStrategy {

    /**
     * 读取队列达到一定数量后开始执行
     */
    private int readLimitSize = 1000;

    /**
     * 检查读取数量的间隔
     */
    private long checkInterval = 10000;

    private CheckReadThread checkThread;

    private final Object locker = new Object();

    private volatile boolean open = false;

    public LimitReadHouseKeepingStrategy(QueueMiddleComponent adapter) {
        super(adapter);
    }

    @Override
    public void open() {
        open = true;
        checkThread = new CheckReadThread();
        checkThread.start();
    }

    @Override
    public void close() throws IOException {
        open = false;
        synchronized (this.locker) {
            this.locker.notifyAll();
        }
    }

    public int getReadLimitSize() {
        return readLimitSize;
    }

    public void setReadLimitSize(int readLimitSize) {
        this.readLimitSize = readLimitSize;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    class CheckReadThread extends Thread {

        public CheckReadThread() {
            super("hp-check-limit-thread");
        }

        @Override
        public void run() {
            while (open) {
                try {
                    synchronized (locker) {
                        locker.wait(checkInterval);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                if (!open) {
                    break;
                }
                long popCount = adapter.popCount();
                if (popCount >= readLimitSize) {
                    try {
                        adapter.houseKeeping();
                        if (logger.isDebugEnabled()) {
                            logger.debug("house keeping success. snapshot pop count:" + popCount);
                        }
                    } catch (PersistenceException e) {
                        logger.error("house keeping failure", e);
                    }
                }
            }
        }
    }

}
