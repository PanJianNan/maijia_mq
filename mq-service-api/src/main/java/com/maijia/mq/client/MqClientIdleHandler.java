package com.maijia.mq.client;

import com.maijia.mq.AckPingPong;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 客户端Idler Handler
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqClientIdleHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MqClientIdleHandler.class);

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
				case READER_IDLE:
					handleReaderIdle(ctx);
					break;
				case WRITER_IDLE:
					handleWriterIdle(ctx);
					break;
				case ALL_IDLE:
					handleAllIdle(ctx);
					break;
				default:
					break;
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	protected void handleReaderIdle(ChannelHandlerContext ctx) {
		LOGGER.debug("5秒都没收到消息，可能消息队列为空，服务端阻塞了并等待新的消息产生");
	}

	protected void handleWriterIdle(ChannelHandlerContext ctx) {
		ctx.writeAndFlush(AckPingPong.PING);
	}

	protected void handleAllIdle(ChannelHandlerContext ctx) {

	}
}
