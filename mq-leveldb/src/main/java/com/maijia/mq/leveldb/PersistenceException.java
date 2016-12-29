package com.maijia.mq.leveldb;

/**
 * 持久化异常
 *
 * @author panjn
 * @date 2016/12/12
 */
public class PersistenceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6108849590042130844L;

    public PersistenceException(String msg) {
        super(msg);
    }

    public PersistenceException(Exception e) {
        super(e);
    }
}
