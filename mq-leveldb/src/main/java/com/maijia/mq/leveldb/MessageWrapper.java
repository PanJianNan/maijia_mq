package com.maijia.mq.leveldb;

import java.io.Serializable;

/**
 * it wrapper message and a txn id
 *
 * @author panjn
 * @date 2016/12/16
 */
public class MessageWrapper<E> implements Serializable {

    private static final long serialVersionUID = -8753164406257202610L;

    private final String txnId;

    private final E message;

    public MessageWrapper(String txnId, E message) {
        this.txnId = txnId;
        this.message = message;
    }

    public String getTxnId() {
        return txnId;
    }

    public E getMessage() {
        return message;
    }
}
