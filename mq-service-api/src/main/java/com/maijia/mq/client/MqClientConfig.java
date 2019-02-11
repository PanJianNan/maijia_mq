package com.maijia.mq.client;

import com.maijia.mq.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MqClientConfig
 *
 * @author panjn
 * @date 2019/1/23
 */
public class MqClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqClientConfig.class);

    public static String HOST;
    public static final String ZK_CONNECT;

    static {
        //加载配置文件
        InputStream inputStream = MqClientConfig.class.getClassLoader().getResourceAsStream("mjmq.properties");
        if (inputStream == null) {
            throw new RuntimeException("cant't find mjmq.properties");
        } else {
            try (InputStream in = new BufferedInputStream(inputStream)) {
                Properties prop = new Properties();
                prop.load(in);

//                HOST = prop.getProperty("host");
//                if (HOST == null || !CommonUtils.isIp(HOST)) {
//                    throw new RuntimeException("please set 'host' correctly in mjmq.properties");
//                }

                ZK_CONNECT = prop.getProperty("zookeeper.connect", "localhost:2181");
            } catch (FileNotFoundException e) {
//                LOGGER.warn("missing mjmq.properties");
                throw new RuntimeException("cant't find mjmq.properties");
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException("exception when loading mjmq.properties");
            }
        }
    }

}
