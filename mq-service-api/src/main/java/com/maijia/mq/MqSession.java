package com.maijia.mq;

import io.netty.channel.Channel;

/**
 * 会话Session
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqSession {

    //Session的唯一标识
    private String id;
    //和Session相关的channel,通过它向客户端回送数据
    private Channel channel = null;
    //上次通信时间
    private long lastCommunicateTimeStamp = 0l;

    //消息队列名（一次会话只能请求一个消息队列）
    private String queueName;

    public MqSession(Channel channel) {
        this.channel = channel;

        //此处暂且使用netty生成的类似UUID的字符串,来标识一个session
        this.id = channel.id().asLongText();
        this.lastCommunicateTimeStamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastCommunicateTimeStamp() {
        return lastCommunicateTimeStamp;
    }

    public void setLastCommunicateTimeStamp(long lastCommunicateTimeStamp) {
        this.lastCommunicateTimeStamp = lastCommunicateTimeStamp;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}