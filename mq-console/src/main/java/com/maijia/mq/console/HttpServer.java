package com.maijia.mq.console;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

import com.maijia.mq.console.pipelinefactory.HttpChannelPipelineFactory;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * Http服务器
 */
public class HttpServer {
    //服务端启动类
    private static final Logger LOGGER = Logger.getLogger(HttpServer.class);

    //creates a new server-side Channel and accepts incoming connections
    public static ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
    //管道管理组
    public static ChannelGroup allChannels = new DefaultChannelGroup();

    /**
     * 服务端初始化方法
     *
     * @param port
     */
    public static void init(int port) {
        //sets the ChannelPipelineFactory which creates a new ChannelPipeline for each new Channel
        bootstrap.setPipelineFactory(new HttpChannelPipelineFactory());

        //sets an option with specified key and value

        bootstrap.setOption("child.tcpNoDelay", Boolean.TRUE);
        bootstrap.setOption("child.keepAlive", Boolean.FALSE);

        //creates a new channel which is bound to the specified local address
        int portNum = port == 0 ? 10241 : port;//todo 临时
        Channel channel = bootstrap.bind(new InetSocketAddress(portNum));

        allChannels.add(channel);
        LOGGER.info("HTTP服务端启动，IP为" + Util.getLocalHost() + "，端口为" + portNum);

    }

    /**
     * 关闭netty 服务器
     */
    public static void shutDown() {
        ChannelGroupFuture future = allChannels.close();
        future.awaitUninterruptibly();
        bootstrap.releaseExternalResources();
    }

    /**
     * 将参数传回请求方
     *
     * @param e
     * @param parameterStr
     */
    public static void writeResponse(MessageEvent e, String parameterStr) {
        ChannelBuffer buf = ChannelBuffers.copiedBuffer((CharSequence) parameterStr, Charset.forName("UTF-8"));

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
        response.setHeader("Cache-Control", "no-cache");

        ChannelFuture future = e.getChannel().write(response);

        future.addListener(ChannelFutureListener.CLOSE);
    }
}
