package com.maijia.mq.leveldb;

import java.io.Closeable;

/**
 * House Keeping
 *
 * @author panjn
 * @date 2016/12/12
 */
public interface IHouseKeepingStrategy extends Closeable {

    /**
     * open house keeping schedule
     */
    void open();
}
