package com.maijia.mq.console;

import com.maijia.mq.MjMqProtocolDecoder;
import com.maijia.mq.MjMqProtocolEncoder;
import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.producer.Producer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://blog.csdn.net/wocjy/article/details/78661464
 */
public class MqServerThread implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqServerThread.class);

    private int port;
    private Consumer consumer;
    private Producer producer;

    public MqServerThread(int port, Consumer consumer, Producer producer) {
        this.port = port;
        this.consumer = consumer;
        this.producer = producer;
    }

    @Override
    public void run() {
        //NioEventLoopGroup是一个处理I/O操作的多线程事件循环
        //bossGroup作为boss,接收传入连接
        //bossGroup只负责接收客户端的连接，不做复杂操作，为了减少资源占用，取值越小越好
        //Group：群组，Loop：循环，Event：事件，这几个东西联在一起，相比大家也大概明白它的用途了。
        //Netty内部都是通过线程在处理各种数据，EventLoopGroup就是用来管理调度他们的，注册Channel，管理他们的生命周期。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //workerGroup作为worker，处理boss接收的连接的流量和将接收的连接注册进入这个worker
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //ServerBootstrap负责建立服务端
            //你可以直接使用Channel去建立服务端，但是大多数情况下你无需做这种乏味的事情
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    //指定使用NioServerSocketChannel产生一个Channel用来接收连接
                    .channel(NioServerSocketChannel.class)
                    //ChannelInitializer用于配置一个新的Channel
                    //用于向你的Channel当中添加ChannelInboundHandler的实现
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(10, 0, 0));
                            ch.pipeline().addLast(new MjMqProtocolDecoder(65536, 1, 2));
                            ch.pipeline().addLast(new MjMqProtocolEncoder());
                            ch.pipeline().addLast(new MqServerIdleHandler());
                            ch.pipeline().addLast(new MqServerHandler(consumer, producer));
                        }

                    })
                    //对Channel进行一些配置
                    //注意以下是socket的标准参数
                    //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    //Option是为了NioServerSocketChannel设置的，用来接收传入连接的
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    //childOption是用来给父级ServerChannel之下的Channels设置参数的
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//                    .childOption(ChannelOption.AUTO_READ, true);//设为false的话，客户端连接异常关闭，服务端会感知不到，造成大量CLOSE_WAIT现象

            // 绑定并开始接受传入的连接。
            ChannelFuture channelFuture = b.bind(port).sync();

            //等到服务器socket关闭。
            // 在这个例子中，这种情况不会发生，但你可以优雅的做到这一点
            // 关闭服务器.
            //sync()会同步等待连接操作结果，用户线程将在此wait()，直到连接操作完成之后，线程被notify(),用户代码继续执行
            //closeFuture()当Channel关闭时返回一个ChannelFuture,用于链路检测
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            //资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
