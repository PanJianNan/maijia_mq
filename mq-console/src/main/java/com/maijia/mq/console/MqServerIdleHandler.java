package com.maijia.mq.console;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.AckPingPong;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

/**
 * 服务端端Idler Handler
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqServerIdleHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MqServerIdleHandler.class);

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
		LOGGER.info("客户端10秒内未发起请求，释放连接：" + JSONObject.toJSONString(ctx.channel().remoteAddress()));
		ctx.close();
	}

	protected void handleWriterIdle(ChannelHandlerContext ctx) {

	}

	protected void handleAllIdle(ChannelHandlerContext ctx) {

	}
}
