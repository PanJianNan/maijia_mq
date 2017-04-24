package com.maijia.mq.console;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Main class that can bootstrap an MaijiaMQ broker console.
 *
 * @author panjn
 * @date 2017/1/10
 */
public class Main {

    public static void main(String[] args) {
        startServer(args);
    }

    /**
     * Start server
     *
     * @param args
     */
    public static void startServer(String[] args) {
        DOMConfigurator.configureAndWatch("_log4j.xml");//初始化log4j todo how to load log4j setting manual
//        PropertyConfigurator.configure("_log4j.xml");//另一种方式初始化log4j

        ServerManager serverManager = ServerManager.getInstance();//获取服务器管理实例

        serverManager.startServer(args);//启动服务器
    }
}
