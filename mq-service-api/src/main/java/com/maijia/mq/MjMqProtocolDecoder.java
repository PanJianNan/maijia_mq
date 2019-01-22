package com.maijia.mq;

import com.maijia.mq.util.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * MJMQ传输协议解析
 *
 * @author panjn
 * @date 2019/1/18
 */
public class MjMqProtocolDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 头部信息的大小应该是 1byte magic + 1byte lengthHigh + 1byte lengthLow = 3
     */
    private static final int HEADER_SIZE = 3;

    public MjMqProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    /**
     * @param maxFrameLength      帧的最大长度
     * @param lengthFieldOffset   length字段偏移的地址
     * @param lengthFieldLength   length字段所占的字节长
     * @param lengthAdjustment    修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
     * @param initialBytesToStrip 解析时候跳过多少个长度
     * @param failFast            为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
     */
    public MjMqProtocolDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null) {
            return null;
        }

        //这个HEADER_LENGTH是我们用于表示头部长度的字节数
        if (in.readableBytes() <= HEADER_SIZE) {
            return null;
        }

        //我们标记一下当前的readIndex的位置
        in.markReaderIndex();

        byte magic = in.readByte();
        byte dataLengthHigh = in.readByte();
        byte dataLengthLow = in.readByte();

        int dataLength = NettyUtils.bytesToInt(dataLengthHigh, dataLengthLow);

        if (in.readableBytes() < dataLength) {
            //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return null;
        }

        //这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
        byte[] data = new byte[dataLength];
        in.readBytes(data);


//        Object obj = HessianSerializerUtil.deserialize(data);
        //还是不要解析成对象返回的好，这样收到信息的一方可以获取这个协议的信息，如内容长度等，内容的反序列化交由接收方处理
        MjMqProtocol protocol = new MjMqProtocol(magic, dataLengthHigh, dataLengthLow, data);

        return protocol;
    }
}