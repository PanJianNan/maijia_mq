package com.maijia.mq;

import org.apache.log4j.Logger;

/**
 * CommonThreadExceptionhandler
 *
 * @author panjn
 * @date 2019/1/18
 */
public class CommonThreadExceptionhandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(CommonThreadExceptionhandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error(e.getMessage(), e);
    }
}
