package com.maijia.mq.console;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器管理类，用户安全启动和关闭服务器
 *
 * @author panjn
 * @date 2017/1/11
 */
public class ServerManager {

    private final Logger logger = Logger.getLogger(this.getClass());

    private static volatile boolean serverStart = false;

    private Map<String, String> param = null;

    private ServerManager() {

    }

    /**
     * 获取服务器管理单例
     *
     * @return ServerManager
     */
    public static ServerManager getInstance() {
        return ServerManagerSingleton.instance;
    }

    /**
     * 初始化Spring上下文
     */
    private void initSpringContext() {
        logger.info("初始化Spring上下文！");
        long s = System.currentTimeMillis();
        SpringContext.initSpringContext();//初始化Spring容器
        long e = System.currentTimeMillis();
        logger.info("初始化Spring上下文成功，耗时：" + (e - s) + "ms！");
    }

    /**
     * 启动netty服务器
     */
    private void initHttpServer(int port) {
        logger.info("启动netty服务器！");
        long s = System.currentTimeMillis();
        //端口号在核心模块配置,todo 暂时写死10241
        Thread httpServerThread = new Thread(new HttpServer(10241), "httpServerThread");
        httpServerThread.start();
        long e = System.currentTimeMillis();
        logger.info("启动netty服务器成功，耗时：" + (e - s) + "ms！");
    }

    /**
     * 初始化线程池或线程
     */
    private void initThreadPool() {
//        ThreadHolder.init();//框架线程池
    }

    /**
     * 初始化业务参数
     */
    private void initParams() {
        logger.info("初始化各模块业务参数……");
        long s = System.currentTimeMillis();
//		InitParamUtil.init();
//		SchedulerManager.init();
        long e = System.currentTimeMillis();
        logger.info("初始化各模块业务参数成功，耗时：" + (e - s) + "ms！");
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
//        HttpServer.bootstrap.shutdown();
    }

    /**
     * 启动服务器
     */
    public void startServer(String args[]) {
        param = resolveParam(args);
        logger.info("服务器开始启动！");
        this.printJvmInfo();
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());//关闭服务器时的回调钩子

        long s0 = System.currentTimeMillis();

        this.initSpringContext();//初始化spring容器
        this.initHttpServer(Util.getIntOfObj(param.get("port")));//初始化netty服务器

        this.initParams();//初始化业务参数

        //改为由InitNioThread被Spring加载后启动
        //this.initNIO();//初始化NIO

        //初始化netty nio线程，用于处理客户端消息请求
        //this.initMqListenThread();//(同样改为交由Spring初始化)

        //this.initThreadPool();//初始化线程池

        this.printJvmInfo();
        long s1 = System.currentTimeMillis();
        logger.info("服务器启动成功，耗时：" + (s1 - s0) + "毫秒！");
        serverStart = true;
    }

    /**
     * 初始化netty nio线程，用于处理客户端消息请求
     */
    /*private void initMqListenThread() {

    }*/

    /**
     * 初始化NIO
     */
    /*private void initNIO() {
        logger.info("init NIO thread!");
        NioMonitorThread thread = new NioMonitorThread();
        thread.setName("NIO monitor thread!");
        thread.start();
    }*/

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
        logger.info("JVM 可获得的最大内存大小：" + Util.parseDoubleToStr(JvmInfo.getMaxMemory() / (double) 1024, "#.##") + " MB!");
        logger.info("JVM 当前已分配到的内存大小：" + Util.parseDoubleToStr(JvmInfo.getTotalMemory() / (double) 1024, "#.##") + " MB!");
        logger.info("JVM 当前可用的内存大小：" + Util.parseDoubleToStr(JvmInfo.getFreeMemory() / (double) 1024, "#.##") + " MB!");
        JvmInfo.getJDKInfo();
    }

    public void printJDKinfo() {
        logger.info("JDK 信息：" + JvmInfo.getJDKInfo());
    }

    private Map<String, String> resolveParam(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String[] kV = args[i].split(":");
            map.put(kV[0], kV[1]);
        }
        return map;
    }

    public static boolean isServerStart() {
        return serverStart;
    }

    /**
     * 使用静态内部类实现的[单例模式]
     */
    private static class ServerManagerSingleton {
        private static final ServerManager instance = new ServerManager();
    }
}
