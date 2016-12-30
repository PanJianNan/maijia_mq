package com.maijia.mq.leveldb;

import com.maijia.mq.domain.Message;

import java.io.Serializable;

/**
 * It wrapper message and a message id
 *
 * @author panjn
 * @date 2016/12/16
 */
public class MessageWrapper implements Serializable {

    private static final long serialVersionUID = -8753164406257202610L;

    private final String msgId;

    private final Message message;

    public MessageWrapper(String msgId, Message message) {
        this.msgId = msgId;
        this.message = message;
    }

    public String getMsgId() {
        return msgId;
    }

    public Message getMessage() {
        return message;
    }
}
