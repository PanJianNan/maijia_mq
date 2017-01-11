package com.maijia.mq.console;


import org.apache.log4j.Logger;

/**
 * 关闭服务器时的回调钩子
 *
 * @author panjn
 * @date 2017/1/11
 */
public class ShutdownHook extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ShutdownHook.class);

    @Override
    public void run() {
        LOGGER.info("shut down the server!");
        ServerManager serverManager = ServerManager.getInstance();//获取服务器管理实例
        serverManager.shutDownThreadPool();
        serverManager.shutDownNetty();
    }
}
