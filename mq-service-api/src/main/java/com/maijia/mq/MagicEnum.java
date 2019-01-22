package com.maijia.mq;

/**
 * MjMqProtocol.magic
 *
 * @author panjn
 * @date 2019/1/22
 */
public enum MagicEnum {
    MESSAGE((byte) 1), ACK((byte) 2), PING((byte) 3), PONG((byte) 4);

    private byte value;

    MagicEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
