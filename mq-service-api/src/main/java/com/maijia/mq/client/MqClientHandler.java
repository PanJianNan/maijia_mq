package com.maijia.mq.client;

import com.maijia.mq.MjMqProtocol;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.util.HessianSerializeUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * 客户端端消息请求处理Handler
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MqClientHandler.class);
	private String queueName;
	private MQConsumer mqConsumer;

	private boolean loopRequset = false;

	public MqClientHandler(String queueName, MQConsumer mqConsumer) {
		this.queueName = queueName;
		this.mqConsumer = mqConsumer;
	}

	public MqClientHandler(String queueName, MQConsumer mqConsumer, boolean loopRequset) {
		this.queueName = queueName;
		this.mqConsumer = mqConsumer;
		this.loopRequset = loopRequset;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		//向服务端请求消息数据
		ctx.writeAndFlush(queueName);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ChannelReadThread channelReadThread = new ChannelReadThread(ctx, msg);
		ClientThreadPoolHolder.mqClientHandlerPool.execute(channelReadThread);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		LOGGER.error(cause.getMessage(), cause);
		ctx.close();
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
				MjMqProtocol protocol = (MjMqProtocol) msg;
				byte[] data = protocol.getContent();

				Object obj = HessianSerializeUtils.deserialize(data);
				if (obj instanceof Exception) {
					LOGGER.error(((Exception) obj).getMessage(), (Exception) obj);
					throw (Exception) obj;
				}

				//进行消费
				mqConsumer.handleDelivery((Message) obj);//todo 可能会造成阻塞，如果客户操作过重的话，导致线程资源占用

				ctx.writeAndFlush("@success@").addListener((ChannelFuture writeFuture) -> {
					//消息发送成功
					if (writeFuture.isSuccess()) {
						if (loopRequset) {
							//继续向服务端请求消息数据
							ctx.writeAndFlush(queueName);
						} else {
							//close channel
							writeFuture.channel().close();
						}
					}
					//消息发送失败
					else {
						//ChannelUtils.closeOnFlush(ctx.channel());
					}
				});

			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				ctx.close();//todo 先关闭
			}
		}
	}
}
