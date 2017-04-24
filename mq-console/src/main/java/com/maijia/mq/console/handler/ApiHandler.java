package com.maijia.mq.console.handler;

import com.maijia.mq.console.HttpServer;
import com.maijia.mq.console.PrimaryFilterChain;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


/**
 * api最外层接口，用于接收客户端请求
 * 框架暂时只支持post方式请求，并且参数类型为非空json字符串
 *
 * @author cjx
 */
public class ApiHandler extends SimpleChannelUpstreamHandler {

    public static final Logger logger = Logger.getLogger(ApiHandler.class.getName());

    /**
     * 当接收到消息时，自动调用该方法
     *
     * @param ctx
     * @param e
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {

        //初级过滤
        PrimaryFilterChain primaryFilterChain = PrimaryFilterChain.getInstance();
        primaryFilterChain.filter(e);
    }

    /**
     * 当管道被接通时自动调用该方法
     *
     * @param ctx
     * @param e
     */
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelConnected(ctx, e);
    }

    /**
     * 当出现异常时，自动调用该方法抛出异常
     *
     * @param ctx
     * @param e
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {

        if (e.getChannel() != null) {
            e.getChannel().close();
        }
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {

        HttpServer.allChannels.add(e.getChannel());
    }

}
