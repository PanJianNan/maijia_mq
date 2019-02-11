package com.maijia.mq.client;

import com.maijia.mq.constant.CommonConstant;
import com.maijia.mq.rpc.RpcFramework;
import com.maijia.mq.service.ICacheMqService;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.IFileMqService;
import com.maijia.mq.service.IMqService;

import java.io.IOException;

/**
 * ConnectionFactory
 *
 * @author panjn
 * @date 2017/1/2
 */
public class ConnectionFactory {

    /**
     * Default user name
     */
    public static final String DEFAULT_USER = "guest";
    /**
     * Default password
     */
    public static final String DEFAULT_PASS = "guest";
    /**
     * The default host
     */
    public static final String DEFAULT_HOST = "localhost";
    /**
     * The default factory mode
     */
    public static final FactoryMode DEFAULT_MODE = FactoryMode.FILE;

    private String host = DEFAULT_HOST;
    private int port = CommonConstant.NIO_RPC_PORT;
    private FactoryMode mode = DEFAULT_MODE;
    private IMqService mqService;

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setMode(FactoryMode mode) {
        this.mode = mode;
    }

    public FactoryMode getMode() {
        return mode;
    }

    public IMqService getMqService() {
        return mqService;
    }

    public void setMqService(IMqService mqService) {
        this.mqService = mqService;
    }

    /**
     * Create a new broker connection.
     *
     * @return
     * @throws IOException
     */
    public Connection newConnection() throws IOException {

        int mqRequestPort = -1;
        String defaultVersion = "1.0.0";
        switch (mode) {
            case FAST:
                mqService = RpcFramework.refer(IFastMqService.class, host, port, defaultVersion);
                mqRequestPort = CommonConstant.FAST_MQ_LISTEN_PORT;
                break;
            case CACHE:
                mqService = RpcFramework.refer(ICacheMqService.class, host, port, defaultVersion);
                mqRequestPort = CommonConstant.CACHE_MQ_LISTEN_PORT;
                break;
            case FILE:
                mqService = RpcFramework.refer(IFileMqService.class, host, port, defaultVersion);
                mqRequestPort = CommonConstant.FILE_MQ_LISTEN_PORT;
                break;
            default:
                mqRequestPort = CommonConstant.FILE_MQ_LISTEN_PORT;
                mqService = RpcFramework.refer(IFileMqService.class, host, port, defaultVersion);
                break;
        }

        Connection connection = new Connection();
        connection.setHost(host);
        connection.setPort(mqRequestPort);
        return connection;
    }

}
