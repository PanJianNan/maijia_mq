package com.maijia.mq;

import com.maijia.mq.util.HessianSerializeUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * MJMQ协议encoder
 *
 * @author panjn
 * @date 2019/1/18
 */
public class MjMqProtocolEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object object, ByteBuf out) throws Exception {
        //将内容进行序列化
        byte[] data = HessianSerializeUtils.serialize(object);
        byte[] bytes = NettyUtils.intToBytes(data.length);

        out.writeByte(bytes[0]);
        out.writeByte(bytes[1]);
        out.writeBytes(data);
    }
}