package com.maijia.mq.leveldb;

import com.maijia.mq.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * leveldb 队列
 * use read cursor and write cursor to offer and transfer data
 *
 * @author panjn
 * @date 2016/12/12
 */
public class LevelDBQueue<E> {

    /**
     * The capacity bound, or Integer.MAX_VALUE if none
     */
    private final int capacity;

    /**
     * Current number of elements
     */
    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * Lock held by take, poll, etc
     */
    private final ReentrantLock takeLock = new ReentrantLock();

    /**
     * Wait queue for waiting takes
     */
    private final Condition notEmpty = takeLock.newCondition();

    /**
     * Lock held by put, offer, etc
     */
    private final ReentrantLock putLock = new ReentrantLock();

    /**
     * Wait queue for waiting puts
     */
    private final Condition notFull = putLock.newCondition();

    private final QueueMiddleComponent<E> adapter;

    private volatile boolean isOpen = false;

    private final Logger logger = Logger.getLogger(this.getClass());

    private IHouseKeepingStrategy houseKeepingStrategy;

    private final Object locker = new Object();

    public LevelDBQueue(QueueMiddleComponent adapter) {
        this(adapter, Integer.MAX_VALUE);
    }

    public LevelDBQueue(QueueMiddleComponent adapter, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;

        this.adapter = adapter;
    }

    public void open() throws IOException {
        isOpen = true;
        this.adapter.open();
        //init queue count from leveldb
        count.getAndSet((int) this.adapter.count());
        if (null == houseKeepingStrategy) {
            // default create Limit ReadHouseKeeping
            houseKeepingStrategy = new LimitReadHouseKeepingStrategy(adapter);
        }
        houseKeepingStrategy.open();
    }

    public void close() throws IOException {
        this.adapter.close();
        this.houseKeepingStrategy.close();
        isOpen = false;
        synchronized (this) {
            this.notifyAll();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary for space to become available.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E msg) throws InterruptedException {
        if (msg == null) {
            throw new NullPointerException();
        }
        // Note: convention in all put/take/etc is to preset local var
        // holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is
             * not protected by lock. This works because count can
             * only decrease at this point (all other puts are shut
             * out by lock), and we (or some other waiting put) are
             * signalled if it ever changes from capacity. Similarly
             * for all other uses of count in other wait guards.
             */
            while (count.get() == capacity) {
                notFull.await();
            }
            adapter.save(msg);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue if it is
     * possible to do so immediately without exceeding the queue's capacity,
     * returning {@code true} upon success and {@code false} if this queue
     * is full.
     * When using a capacity-restricted queue, this method is generally
     * preferable to method {@link BlockingQueue#add add}, which can fail to
     * insert an element only by throwing an exception.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E msg) {
        if (msg == null) {
            throw new NullPointerException();
        }
        final AtomicInteger count = this.count;
        if (count.get() == capacity) {
            return false;
        }
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                adapter.save(msg);
                c = count.getAndIncrement();
                if (c + 1 < capacity) {
                    notFull.signal();
                }
            }
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return c >= 0;
    }

    public E take() throws InterruptedException {
        E x = null;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        System.out.println(adapter.count());
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }
            x = adapter.pop();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = adapter.pop();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = adapter.pop();
                c = count.getAndDecrement();
                if (c > 1) {
                    notEmpty.signal();
                }
            }
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    /**
     * Signals a waiting take. Called only from put/offer (which do not
     * otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Signals a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary up to the specified wait time for space to become available.
     *
     * @return {@code true} if successful, or {@code false} if
     * the specified waiting time elapses before space is available.
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E msg, long timeout, TimeUnit unit) throws InterruptedException {
        if (msg == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {
                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            adapter.save(msg);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } catch (PersistenceException e) {
            logger.error(e.getMessage(), e);
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    public Object transfer() {
        return transfer(-1L);
    }

    public Object transfer(long timeout) {
        if (!isOpen) {
            return null;
        }

        Object evt = null;
        try {
            evt = adapter.pop();
        } catch (PersistenceException e) {
            logger.error("pop message error", e);
        }
        if (evt != null)
            return evt;

        final Object locker = this.locker;
        synchronized (locker) {
            try {
                if (timeout == -1L) {
                    locker.wait();
                } else {
                    locker.wait(timeout);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (timeout == -1L)
            return transfer(timeout);

        if (!isOpen)
            return null;

        try {
            evt = adapter.pop();
        } catch (PersistenceException e) {
            logger.error("pop message error", e);
        }

        return evt;
    }

    public int enqueueSize() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public IHouseKeepingStrategy getHouseKeepingStrategy() {
        return houseKeepingStrategy;
    }

    public void setHouseKeepingStrategy(IHouseKeepingStrategy houseKeepingStrategy) {
        this.houseKeepingStrategy = houseKeepingStrategy;
    }

}
