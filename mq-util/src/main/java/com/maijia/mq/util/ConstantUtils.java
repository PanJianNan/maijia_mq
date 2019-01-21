package com.maijia.mq.util;

/**
 * 常量类
 *
 * @author panjn
 * @date 2016/12/5
 */
public class ConstantUtils {

    /** 缓存key前缀 */
    public static final String CACHE_KEY_PREFIX = "maijia_mq:";

    /** NIO模式PRC端口 */
    public static final int NIO_RPC_PORT = 3198;
    /** NIO模式消息传输端口 */
    public static final int NIO_MSG_TRANSFER_PORT = 3985;

    /** NIO模式消息请求监听端口-FAST */
    public static final int FAST_MQ_LISTEN_PORT = 3986;
    /** NIO模式消息请求监听端口-CHCHE */
    public static final int CACHE_MQ_LISTEN_PORT = 3987;
    /** NIO模式消息请求监听端口-FILE */
    public static final int FILE_MQ_LISTEN_PORT = 3988;

    /** 2个字节能表达的最大整数 */
    public static final int MAX_TWO_BYTES = 32767;

    private ConstantUtils() {
    }

}
