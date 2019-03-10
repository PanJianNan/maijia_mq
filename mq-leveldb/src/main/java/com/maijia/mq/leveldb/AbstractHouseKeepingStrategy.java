package com.maijia.mq.leveldb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractHouseKeepingStrategy
 *
 * @author panjn
 * @date 2016/12/12
 */
public abstract class AbstractHouseKeepingStrategy implements IHouseKeepingStrategy {

    protected final QueueMiddleComponent adapter;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AbstractHouseKeepingStrategy(QueueMiddleComponent adapter) {
        this.adapter = adapter;
    }
}
