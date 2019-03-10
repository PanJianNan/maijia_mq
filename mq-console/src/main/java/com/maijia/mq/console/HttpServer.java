package com.maijia.mq.console;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简易HTTP服务
 *
 * @author panjn
 * @date 2019/1/15
 */
public class HttpServer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int port;

    public HttpServer(int port) {
        this.port = port;
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
//                            ch.pipeline().addLast(new EchoServerHandler());
//
//                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
//                            ch.pipeline().addLast(new HttpResponseEncoder());
//                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
//                            ch.pipeline().addLast(new HttpRequestDecoder());
//                            ch.pipeline().addLast(new HttpServerHandler());
//                            //增加自定义实现的Handler
//                            ch.pipeline().addLast(new HttpServerCodec());

                            // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                            ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                            //将多个消息转换为单一的FullHttpRequest或FullHttpResponse对象
                            ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65535));
                            // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                            ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                            //解决大数据包传输问题，用于支持异步写大量数据流并且不需要消耗大量内存也不会导致内存溢出错误( OutOfMemoryError )。
                            //仅支持ChunkedInput类型的消息。也就是说，仅当消息类型是ChunkedInput时才能实现ChunkedWriteHandler提供的大数据包传输功能
                            ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());//解决大码流的问题
                            ch.pipeline().addLast("http-server",new HttpServerHandler());
                        }

                    })
                    //对Channel进行一些配置
                    //注意以下是socket的标准参数
                    //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    //Option是为了NioServerSocketChannel设置的，用来接收传入连接的
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    //childOption是用来给父级ServerChannel之下的Channels设置参数的
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定并开始接受传入的连接。
            ChannelFuture f = b.bind(port).sync();

            //等到服务器socket关闭。
            // 在这个例子中，这种情况不会发生，但你可以优雅的做到这一点
            // 关闭服务器.
            //sync()会同步等待连接操作结果，用户线程将在此wait()，直到连接操作完成之后，线程被notify(),用户代码继续执行
            //closeFuture()当Channel关闭时返回一个ChannelFuture,用于链路检测
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            //资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        int port = 10241;
        try {
            new HttpServer(port).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
