package com.maijia.mq;

import com.maijia.mq.util.HessianSerializeUtils;
import com.maijia.mq.util.NettyUtils;
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
        //如果是ack或ping/pong心跳包
        if (object instanceof AckPingPong) {
            MagicEnum magicEnum = ((AckPingPong) object).getMagicEnum();
            out.writeByte(magicEnum.getValue());
            out.writeByte(0);
            out.writeByte(1);
            out.writeByte(magicEnum.getValue());
            return;
        }

        //将内容进行序列化
        byte[] data = HessianSerializeUtils.serialize(object);
        byte[] bytes = NettyUtils.intToBytes(data.length);

        out.writeByte(MagicEnum.MESSAGE.getValue());
        out.writeByte(bytes[0]);
        out.writeByte(bytes[1]);
        out.writeBytes(data);
    }
}