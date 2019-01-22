package com.maijia.mq;

/**
 * AckPingPong
 *
 * @author panjn
 * @date 2019/1/22
 */
public class AckPingPong {

    public static final AckPingPong ACK = new AckPingPong(MagicEnum.ACK);
    public static final AckPingPong PING = new AckPingPong(MagicEnum.PING);
    public static final AckPingPong PONG = new AckPingPong(MagicEnum.PONG);

    private MagicEnum magicEnum;

    public AckPingPong(MagicEnum magicEnum) {
        this.magicEnum = magicEnum;
    }

    public MagicEnum getMagicEnum() {
        return magicEnum;
    }

    public void setMagicEnum(MagicEnum magicEnum) {
        this.magicEnum = magicEnum;
    }
}
