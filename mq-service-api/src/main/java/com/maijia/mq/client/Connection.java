package com.maijia.mq.client;

import java.io.Serializable;

/**
 * Connection
 *
 * @author panjn
 * @date 2016/10/26
 */
public class Connection implements Serializable {

    private static final long serialVersionUID = -270496099988459289L;

    private String host = "localhost";
    private int port = -1;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * new channel
     */
    public MqChannel createChannel() {
        return new MqChannel(host, port);
    }

    public void close() {
        //todo
    }
}
