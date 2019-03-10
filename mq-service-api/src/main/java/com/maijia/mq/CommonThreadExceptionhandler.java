package com.maijia.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommonThreadExceptionhandler
 *
 * @author panjn
 * @date 2019/1/18
 */
public class CommonThreadExceptionhandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonThreadExceptionhandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error(e.getMessage(), e);
    }
}
