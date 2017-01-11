package com.maijia.mq.console;

public class JvmInfo {

    /**
     * 获取jvm可用内存大小,单位为KB
     *
     * @return
     */
    public static long getFreeMemory() {
        long b = Runtime.getRuntime().freeMemory();
        return b / 1024;
    }

    /**
     * 获取jvm内存大小，单位为KB
     *
     * @return
     */
    public static long getTotalMemory() {
        long b = Runtime.getRuntime().totalMemory();
        return b / 1024;
    }

    /**
     * 获取jvm内存大小，单位为KB
     *
     * @return
     */
    public static long getMaxMemory() {
        long b = Runtime.getRuntime().maxMemory();
        return b / 1024;
    }

    /**
     * 获取CPU核数
     *
     * @return
     */
    public static int cpuCoreNumber() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static String getJDKInfo() {

        String version = System.getProperty("java.version");
        String bit = System.getProperty("sun.arch.data.model");
        return version + " " + bit + "bit";
    }

}
