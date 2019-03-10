package com.maijia.mq.client;

import com.maijia.mq.MjMqProtocolDecoder;
import com.maijia.mq.MjMqProtocolEncoder;
import com.maijia.mq.service.MQConsumer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * MqClient
 *
 * @author panjn
 * @date 2019/1/22
 */
public class MqClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqClient.class);

    private String queueName;
    private MQConsumer mqConsumer;
    private boolean loopRequest;

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;
    private Channel channel;
    /**
     * 尝试连接次数
     */
    private int attempts;

    public MqClient(String queueName, MQConsumer mqConsumer, boolean loopRequest) {
        this.queueName = queueName;
        this.mqConsumer = mqConsumer;
        this.loopRequest = loopRequest;
    }

    public String getQueueName() {
        return queueName;
    }

    public MQConsumer getMqConsumer() {
        return mqConsumer;
    }

    public boolean isLoopRequest() {
        return loopRequest;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public Channel getChannel() {
        return channel;
    }

    public void connect(String host, int port) throws InterruptedException {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .group(eventLoopGroup)
                .remoteAddress(host, port);

        try {
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new IdleStateHandler(5, 5, 0));
                    socketChannel.pipeline().addLast(new MjMqProtocolDecoder(65536, 1, 2));
                    socketChannel.pipeline().addLast(new MjMqProtocolEncoder());
                    socketChannel.pipeline().addLast(new MqClientIdleHandler());
                    socketChannel.pipeline().addLast(new MqClientHandler(MqClient.this));
                }
            });

//            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();

            doConnect();
        } finally {
//            eventLoopGroup.shutdownGracefully();
        }
    }

    public void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture channelFuture = bootstrap.connect().addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                channel = future.channel();
                LOGGER.info("------connect server success------");
            } else {
                attempts++;
                int delaySeconds;
                if (attempts < 5) {
                    //重连的间隔时间会越来越长
                    delaySeconds = (int) (5 * Math.pow(2, attempts));
                } else {
                    delaySeconds = 300;
                }
                LOGGER.warn(String.format("Failed to connect to server, try connect after %ds", delaySeconds));
                future.channel().eventLoop().schedule(() -> doConnect(), delaySeconds, TimeUnit.SECONDS);
            }
        });

        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
//            throw e;
        }
    }
}
