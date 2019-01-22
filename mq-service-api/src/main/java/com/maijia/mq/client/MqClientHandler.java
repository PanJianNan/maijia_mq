package com.maijia.mq.client;

import com.maijia.mq.AckPingPong;
import com.maijia.mq.MagicEnum;
import com.maijia.mq.MjMqProtocol;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.util.HessianSerializeUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import org.apache.log4j.Logger;

/**
 * 客户端端消息请求处理Handler
 *
 * @author panjn
 * @date 2019/1/17
 */
public class MqClientHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOGGER = Logger.getLogger(MqClientHandler.class);

	private MqClient mqClient;

	private String queueName;
	private MQConsumer mqConsumer;
	private boolean loopRequset = false;

	private EventLoopGroup eventLoopGroup;

	public MqClientHandler(MqClient mqClient) {
		this.mqClient = mqClient;
		this.queueName = mqClient.getQueueName();
		this.mqConsumer = mqClient.getMqConsumer();
		this.loopRequset = mqClient.isLoopRequest();

		this.eventLoopGroup = mqClient.getEventLoopGroup();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		//向服务端请求消息数据
		ctx.writeAndFlush(queueName);
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (loopRequset) {
			mqClient.doConnect();
		} else {
			eventLoopGroup.shutdownGracefully();
		}
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		MjMqProtocol protocol = (MjMqProtocol) msg;
		if (protocol.getMagic() == MagicEnum.PING.getValue() || protocol.getMagic() == MagicEnum.PONG.getValue())  {
			//心跳包暂不处理
			return;
		}
		ChannelReadThread channelReadThread = new ChannelReadThread(ctx, protocol);
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
		private MjMqProtocol protocol;

		public ChannelReadThread(ChannelHandlerContext ctx, MjMqProtocol protocol) {
			this.ctx = ctx;
			this.protocol = protocol;
		}

		@Override
		public void run() {
			try {
				byte[] data = protocol.getContent();

				Object obj = HessianSerializeUtils.deserialize(data);
				if (obj instanceof Exception) {
					LOGGER.error(((Exception) obj).getMessage(), (Exception) obj);
					throw (Exception) obj;
				}

				//进行消费
				mqConsumer.handleDelivery((Message) obj);//todo 可能会造成阻塞，如果客户操作过重的话，导致线程资源占用

				ctx.writeAndFlush(AckPingPong.ACK).addListener((ChannelFuture writeFuture) -> {
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
