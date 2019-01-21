package com.maijia.mq;

import com.maijia.mq.util.ConstantUtils;

/**
 * NettyUtils
 *
 * @author panjn
 * @date 2019/1/18
 */
public class NettyUtils {

    /**
     * 将int转为高字节在前，低字节在后的2字节数组
     *
     * @param n int 被转化整数
     * @return byte[] 转化后的结果
     */
    public static byte[] intToBytes(int n) {
        if (n < 0 || n > ConstantUtils.MAX_TWO_BYTES) {
            throw new IllegalArgumentException("被转化整数值需要在0和32767之间");
        }
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) (n >> 8 & 0xff);
        return b;
    }

    /**
     * 将2字节转换为整数
     *
     * @param highByte 高字节
     * @param lowByte  低字节
     * @return
     */
    public static int bytesToInt(byte highByte, byte lowByte) {
        int value;
        value = (((highByte & 0xFF) << 8) | (lowByte & 0xFF));
        return value;
    }

}
