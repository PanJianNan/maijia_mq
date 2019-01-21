package com.maijia.mq.console;

import com.maijia.mq.CommonThreadExceptionhandler;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadHolder
 *
 * @author panjn
 * @date 2019/1/17
 */
public class ThreadPoolHolder {

    public static ThreadPoolExecutor mqServerHandlerPool = new ThreadPoolExecutor(10, 30,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ServerThreadFactory());


    private static class ServerThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ServerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "mq-server-pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);

            t.setUncaughtExceptionHandler(new CommonThreadExceptionhandler());
            return t;
        }
    }
}
