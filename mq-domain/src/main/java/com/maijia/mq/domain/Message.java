package com.maijia.mq.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Message
 *
 * @author panjn
 * @date 2016/12/30
 */
public class Message<E> implements Serializable {
    /**
     * create time 时间戳
     */
    private long timestamp;

    /**
     * message content
     */
    private E content;

    public Message(E content) {
        this.content = content;
        this.timestamp = new Date().getTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public E getContent() {
        return content;
    }

    public void setContent(E content) {
        this.content = content;
    }

    public static void main(String[] args) {
        Object object = null;
        Message message = (Message) object;
        System.out.println(message);


    }
}
