package com.maijia.mq.leveldb;

import org.apache.log4j.Logger;


/**
 * AbstractHouseKeepingStrategy
 *
 * @author panjn
 * @date 2016/12/12
 */
public abstract class AbstractHouseKeepingStrategy implements IHouseKeepingStrategy {

    protected final QueueMiddleComponent adapter;

    protected final Logger logger = Logger.getLogger(this.getClass());

    public AbstractHouseKeepingStrategy(QueueMiddleComponent adapter) {
        this.adapter = adapter;
    }
}
