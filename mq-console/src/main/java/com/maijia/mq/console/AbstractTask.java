package com.maijia.mq.console;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractTask<T> implements ITask {

    final protected static Log logger = LogFactory.getLog(AbstractTask.class);

    protected BlockingQueue<T> taskQueue;//= new ArrayBlockingQueue<T>(1024 * 1024);

    protected Thread thread;

    public void run() {

        thread = Thread.currentThread();
        reNameThread();
        logger.info("启动线程：" + thread.getName());
        processTaskQueue();

    }

    public void registerTask(T task) {
        try {
            taskQueue.put(task);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * @return
     * @Description: 返回任务个数
     */
    public int get任务个数() {
        return taskQueue.size();
    }

    /**
     * 重命名线程
     */
    public abstract void reNameThread();

    /**
     * 从队列取出任务
     */
    public abstract void processTaskQueue();

}
