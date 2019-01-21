package com.maijia.mq.client;

import com.maijia.mq.MjMqProtocolDecoder;
import com.maijia.mq.MjMqProtocolEncoder;
import com.maijia.mq.service.IMqService;
import com.maijia.mq.service.MQConsumer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;

/**
 * 几乎所有的操作都在channel中进行，channel是进行消息读写的通道。客户端可建立多个channel，每个channel代表一个会话任务。
 *
 * @author panjn
 * @date 2016/12/27
 */
public class Channel implements Serializable {

    private final transient Logger logger = Logger.getLogger(this.getClass());

    private String host = "localhost";
    private int port = -1;
    private String queueName;
    private String exchangeName = "default_exchange";
    private ExchangeType exchangeType = ExchangeType.DIRECT;
    private transient IMqService mqService;

    private boolean loopRequest;

    public Channel() {
    }

    public Channel(String host, int port) {
        this.host = host;
        this.port = port;
    }

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

    public String getQueueName() {
        return queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public IMqService getMqService() {
        return mqService;
    }

    public void setMqService(IMqService mqService) {
        this.mqService = mqService;
    }

    public boolean isLoopRequest() {
        return loopRequest;
    }

    public void setLoopRequest(boolean loopRequest) {
        this.loopRequest = loopRequest;
    }

    /**
     * Publish a message
     *
     * @param rawMsg
     * @throws IOException
     * @throws InterruptedException
     */
    public void basicPublish(Object rawMsg) throws IOException, InterruptedException {
        if (rawMsg == null) {
            throw new NullPointerException("message is NULL");
        }
        if (mqService == null) {
            throw new IllegalArgumentException("please set mqService first");
        }

        if (ExchangeType.FANOUT.equals(exchangeType)) {
            mqService.produce(this, rawMsg);
            return;
        }

        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("please declare target queue's name by method queueDeclare(String queueName) first");
        }

        if (ExchangeType.DIRECT.equals(exchangeType)) {
            //register exchange
            mqService.registerExchange(exchangeName, queueName);
        }

        mqService.produce(this, rawMsg);
    }

    /**
     * 消费消息，采用ACK确认机制
     *
     * @param mqConsumer
     */
    public void basicConsume(MQConsumer mqConsumer) throws IOException, InterruptedException {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("please declare target queue's name by method queueDeclare(String queueName) first");
        }
        if (mqService == null) {
            throw new IllegalArgumentException("please set mqService first");
        }

        //register exchange
        mqService.registerExchange(exchangeName, queueName);

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new MjMqProtocolDecoder(65536, 0, 2));
                    socketChannel.pipeline().addLast(new MjMqProtocolEncoder());
                    socketChannel.pipeline().addLast(new MqClientHandler(queueName, mqConsumer, loopRequest));
                }
            });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
//                SocketChannel socketChannel = (SocketChannel) future.channel();
                System.out.println("------connect server success------");
            }
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw e;
//            logger.error(e.getMessage(), e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

    /**
     * Declare target queue'name
     *
     * @param queueName
     */
    public void queueDeclare(String queueName) {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName is blank");
        }
        this.queueName = queueName;
    }

    /**
     * Declare exchange whit default type
     *
     * @param exchangeName
     */
    public void exchangeDeclare(String exchangeName) {
        if (StringUtils.isNotBlank(exchangeName)) {
            this.exchangeName = exchangeName;
        }
    }

    /**
     * Declare exchange and it's type
     * <p/>
     * if exchangeName equals 'default_exchange',then exchangeType remain default type
     *
     * @param exchangeName
     * @param exchangeType
     */
    public void exchangeDeclare(String exchangeName, ExchangeType exchangeType) {
        if (StringUtils.isNotBlank(exchangeName)) {
            this.exchangeName = exchangeName;
        }
        if (exchangeName.equals("default_exchange")) {
            return;
        }
        if (exchangeType != null) {
            this.exchangeType = exchangeType;
        }
    }

    public void close() {
        //todo
    }
}
