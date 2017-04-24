package com.maijia.mq.console;

/**
 * 任务基接口
 */
public interface ITask extends Runnable {
    @Override
    void run();
}
