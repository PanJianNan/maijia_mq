package com.maijia.mq.leveldb;

import java.io.Serializable;

/**
 * It wrapper message and a message id
 *
 * @author panjn
 * @date 2016/12/16
 */
public class MessageWrapper<E> implements Serializable {

    private static final long serialVersionUID = -8753164406257202610L;

    private final String msgId;

    private final E message;

    public MessageWrapper(String msgId, E message) {
        this.msgId = msgId;
        this.message = message;
    }

    public String getMsgId() {
        return msgId;
    }

    public E getMessage() {
        return message;
    }
}
