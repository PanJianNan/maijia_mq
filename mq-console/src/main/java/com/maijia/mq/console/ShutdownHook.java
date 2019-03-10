package com.maijia.mq.console;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 关闭服务器时的回调钩子
 *
 * @author panjn
 * @date 2017/1/11
 */
public class ShutdownHook extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHook.class);

    @Override
    public void run() {
        LOGGER.info("start shut down the server!");
        // 获取服务器管理实例
        ServerManager serverManager = ServerManager.getInstance();
        serverManager.shutDownThreadPool();
        serverManager.shutDownNetty();
        LOGGER.info("shut down the server success!");
    }
}
