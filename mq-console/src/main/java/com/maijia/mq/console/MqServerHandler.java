package com.maijia.mq.console;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.MjMqProtocol;
import com.maijia.mq.MqSession;
import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.domain.Message;
import com.maijia.mq.producer.Producer;
import com.maijia.mq.util.HessianSerializeUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务端消息请求处理Handler
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqServerHandler extends ChannelInboundHandlerAdapter {

    static AtomicInteger aint = new AtomicInteger(0);

    private static final Logger LOGGER = Logger.getLogger(MqServerHandler.class);

    private Consumer consumer;

    private Producer producer;

    Map<String, Object> failMsgMap = new ConcurrentHashMap<>();

    public MqServerHandler(Consumer consumer, Producer producer) {
        this.consumer = consumer;
        this.producer = producer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        LOGGER.debug("服务端开始读取客户端过来的请求");
        ChannelReadThread channelReadThread = new ChannelReadThread(ctx, msg);
        ThreadPoolHolder.mqServerHandlerPool.execute(channelReadThread);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        String channelId = ctx.channel().id().asLongText();

        MqSession session = ctx.channel().attr(AttributeKey.<MqSession>valueOf(channelId)).getAndSet(null);
//
//		// 移除  session 并删除 该客户端
//		SessionManager.getSingleton().removeClient(session, true);

        //todo 暂时将需要重发的消息重新存储起来
        Object msg = failMsgMap.remove(channelId);
        if (msg != null) {
            LOGGER.debug(String.format("客户端未成功消费的消息 msg:%s", JSONObject.toJSONString(msg)));
            producer.produce(session.getQueueName(), msg);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        MqSession session = new MqSession(ctx.channel());
        // 绑定客户端到SOCKET
        String channelId = ctx.channel().id().asLongText();

        ctx.channel().attr(AttributeKey.<MqSession>valueOf(channelId)).set(session);
    }

    private class ChannelReadThread implements Runnable {

        private ChannelHandlerContext ctx;
        private Object msg;

        public ChannelReadThread(ChannelHandlerContext ctx, Object msg) {
            this.ctx = ctx;
            this.msg = msg;
        }

        @Override
        public void run() {

            try {
                // 绑定客户端到SOCKET
                String channelId = ctx.channel().id().asLongText();
                // 检测是否 自己注册的 客户端
                MqSession session = ctx.channel().attr(AttributeKey.<MqSession>valueOf(channelId)).get();

                MjMqProtocol protocol = (MjMqProtocol) msg;

                if (protocol == null || session == null) {
                    LOGGER.error("protocol或session为null");
                    return;
                    //closeConnection(ctx); // 关闭连接
                }

                byte[] data = protocol.getContent();
                String requestStr = HessianSerializeUtils.deserialize(data);

                //确认消息被客户端消费成功
                if ("@success@".equals(requestStr)) {
                    Object obj = failMsgMap.remove(session.getId());
                    LOGGER.debug(String.format("客户端成功消费一条消息 msg:%s", JSONObject.toJSONString(obj)));
                    return;
                }

                //返回消息
                Message message = consumer.take(requestStr);//todo 可能会造成线程等待（parking，处于WAITING状态），这样就会造成线程资源的占用

//                if (message == null) {
//                    System.out.println("null-message-cnt:" + aint.incrementAndGet());
//                    message = new Message("空消息");
//                    ctx.writeAndFlush(message);
//                    LOGGER.debug(String.format("空消息 msg:%s", JSONObject.toJSONString(message)));
//                    return;
//                }
                //备份信息以便消费失败时回滚该消息
                failMsgMap.put(session.getId(), message);
                session.setQueueName(requestStr);//记录本次会话的客户端请求的消息队列名称
                ctx.writeAndFlush(message);

//                LOGGER.debug(String.format("向客户端发送一条消息 msg:%s", JSONObject.toJSONString(message)));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                ctx.close();//发生异常，直接关闭
            }
        }
    }

}
