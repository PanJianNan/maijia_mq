package com.maijia.mq.console;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器管理类，单例+构造器设计模式，用户安全启动和关闭服务器
 *
 * @author panjn
 * @date 2017/1/11
 */
public class ServerManager {

    private static final Logger LOGGER = Logger.getLogger(ServerManager.class);

    private static volatile boolean isServerStart = false;

    private Map<String, String> param = null;
    /**
     * 单例实例
     */
    private static ServerManager serverManager = null;

    private ServerManager() {

    }

    /**
     * 获取实例
     *
     * @return ServerManager
     */
    public static ServerManager getInstance() {
        if (serverManager == null) {
            serverManager = new ServerManager();
        }
        return serverManager;
    }

    /**
     * 初始化Spring容器
     */
    private void initSpringContext() {
        LOGGER.info("初始化Spring容器！");
        long s = System.currentTimeMillis();
        SpringContext.initSpringContext();//初始化spring容器
        long e = System.currentTimeMillis();
        LOGGER.info("初始化Spring容器成功，耗时：" + (e - s) + "ms！");
    }

    /**
     * 启动netty服务器
     */
    private void initHttpServer(int port) {
        LOGGER.info("启动netty服务器！");
        long s = System.currentTimeMillis();
        //端口号在核心模块配置
        HttpServer.init(port);
        long e = System.currentTimeMillis();
        LOGGER.info("启动netty服务器成功，耗时：" + (e - s) + "ms！");
    }

    /**
     * 初始化线程池或线程
     */
    private void initThreadPool() {
        ThreadHolder.init();//框架线程池
        //AsynThreadHolder.init();//业务线程池
    }

    /**
     * 初始化业务参数
     */
    private void initParams() {
        LOGGER.info("初始化各模块业务参数……");
        long s = System.currentTimeMillis();
//		InitParamUtil.init();
//		SchedulerManager.init();
        long e = System.currentTimeMillis();
        LOGGER.info("初始化各模块业务参数成功，耗时：" + (e - s) + "ms！");
    }

    /**
     * 安全关闭线程池
     */
    public void shutDownThreadPool() {
//		((IPool)ThreadHolder.quickThreadTask).shutDownPool();
//		((IPool)ThreadHolder.slowThreadTask).shutDownPool();
//		((IPool)AsynThreadHolder.asynchronizedServiceTask).shutDownPool();
//		((IPool)AsynThreadHolder.logTask).shutDownPool();
    }

    /**
     * 关闭netty服务器
     */
    public void shutDownNetty() {
        HttpServer.bootstrap.shutdown();
    }

    /**
     * 启动服务器
     */
    public void startServer(String args[]) {
        param = resolveParam(args);
        LOGGER.info("服务器开始启动！");
        this.printJDKinfo();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        long s0 = System.currentTimeMillis();

        this.initSpringContext();//初始化spring容器
        this.initHttpServer(Util.getIntOfObj(param.get("port")));//初始化netty服务器
        this.initThreadPool();//初始化线程池

        this.initParams();//

        this.printJvmInfo();
        long s1 = System.currentTimeMillis();
        LOGGER.info("服务器启动成功，耗时：" + (s1 - s0) + "毫秒！");
        isServerStart = true;
    }

    /**
     * 关闭服务器
     */
    public void shutDownServer() {
        System.exit(0);
    }

    /**
     * 重启服务器
     */
    public void reStartServer() {
        //todo
    }

    public void printJvmInfo() {
        LOGGER.info("JVM 可获得的最大内存大小：" + Util.parseDoubleToStr(JvmInfo.getMaxMemory() / (double) 1024, "#.##") + " MB!");
        LOGGER.info("JVM 当前已分配到的内存大小：" + Util.parseDoubleToStr(JvmInfo.getTotalMemory() / (double) 1024, "#.##") + " MB!");
        LOGGER.info("JVM 当前可用的内存大小：" + Util.parseDoubleToStr(JvmInfo.getFreeMemory() / (double) 1024, "#.##") + " MB!");
        LOGGER.info("JDK 信息：" + JvmInfo.getJDKInfo());
    }

    public void printJDKinfo() {
//        log.info("JDK 信息：" + JvmInfo.getJDKInfo());
    }

    private Map<String, String> resolveParam(String[] args) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            String[] kV = args[i].split(":");
            map.put(kV[0], kV[1]);
        }
        return map;
    }

    public static boolean isServerStart() {
        return isServerStart;
    }
}
