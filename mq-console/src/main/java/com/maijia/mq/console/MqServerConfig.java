package com.maijia.mq.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * MqServerConfig
 *
 * @author panjn
 * @date 2019/2/3
 */
public class MqServerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqServerConfig.class);

    public static final boolean USE_REDIS;
    public static final boolean ZK_INTEGRATION;
    public static final String ZK_CONNECT;

    static {
        //加载配置文件
        InputStream inputStream = SpringContext.class.getClassLoader().getResourceAsStream("conf" + File.separator + "mjmq-server.properties");
        if (inputStream == null) {
            throw new RuntimeException("cant't find mjmq-server.properties");
        } else {
            try (InputStream in = new BufferedInputStream(inputStream)) {
                Properties prop = new Properties();
                prop.load(in);

                String userRedisStr =  prop.getProperty("useRedis", "false");
                USE_REDIS = Boolean.valueOf(userRedisStr);

                String zkIntegrationStr = prop.getProperty("zookeeper.integration", "true");
                ZK_INTEGRATION = Boolean.valueOf(zkIntegrationStr);

                ZK_CONNECT = prop.getProperty("zookeeper.connect", "localhost:2181");
            } catch (FileNotFoundException e) {
                throw new RuntimeException("cant't find mjmq-server.properties");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException("exception when loading mjmq-server.properties");
            }
        }
    }

}
