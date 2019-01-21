package com.maijia.mq;

/**
 * MJMQ传输协议
 *
 * @author panjn
 * @date 2019/1/18
 */
public class MjMqProtocol {

    /** 内容长度，规定length总共占2个字节，Big Endian, lengthHigh为高字节 */
    private byte lengthHigh;
    /** 内容长度，规定length总共占2个字节，Big Endian，lengthLow为低字节 */
    private byte lengthLow;
    /** 内容 */
    private byte[] content;

    public MjMqProtocol(byte lengthHigh, byte lengthLow, byte[] content) {
        this.lengthHigh = lengthHigh;
        this.lengthLow = lengthLow;
        this.content = content;
    }

    public MjMqProtocol(int length, byte[] content) {
        byte[] bytes = NettyUtils.intToBytes(length);
        this.lengthHigh = bytes[0];
        this.lengthLow = bytes[1];
        this.content = content;
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
