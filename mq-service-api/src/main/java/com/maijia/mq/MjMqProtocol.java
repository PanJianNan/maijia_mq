package com.maijia.mq;

import com.maijia.mq.util.NettyUtils;

/**
 * MJMQ传输协议
 * 自定义消息格式:
 * +-------+------------+-----------+---------+
 * | Magic | LengthHigh | LengthLow | Content |
 * +-------+------------+-----------+---------+
 *
 * @author panjn
 * @date 2019/1/18
 */
public class MjMqProtocol {

    /** 类型  1-消息    2-心跳包ping    3-心跳包pong */
    private byte magic;
    /** 内容长度，规定length总共占2个字节，Big Endian, lengthHigh为高字节 */
    private byte lengthHigh;
    /** 内容长度，规定length总共占2个字节，Big Endian，lengthLow为低字节 */
    private byte lengthLow;
    /** 内容 */
    private byte[] content;

    public MjMqProtocol(byte magic, byte lengthHigh, byte lengthLow, byte[] content) {
        this.magic = magic;
        this.lengthHigh = lengthHigh;
        this.lengthLow = lengthLow;
        this.content = content;
    }

    public MjMqProtocol(byte magic, int length, byte[] content) {
        this.magic = magic;
        byte[] bytes = NettyUtils.intToBytes(length);
        this.lengthHigh = bytes[0];
        this.lengthLow = bytes[1];
        this.content = content;
    }

    public byte getMagic() {
        return magic;
    }

    public void setMagic(byte magic) {
        this.magic = magic;
    }

    public byte getLengthHigh() {
        return lengthHigh;
    }

    public void setLengthHigh(byte lengthHigh) {
        this.lengthHigh = lengthHigh;
    }

    public byte getLengthLow() {
        return lengthLow;
    }

    public void setLengthLow(byte lengthLow) {
        this.lengthLow = lengthLow;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
