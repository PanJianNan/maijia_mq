package com.maijia.mq.client;

import com.maijia.mq.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MqConfig
 *
 * @author panjn
 * @date 2019/1/23
 */
public class MqConfig {

    private static final Logger LOGGER = Logger.getLogger(MqConfig.class);

    public static String host;

    static {
        //加载配置文件
        InputStream inputStream = MqConfig.class.getClassLoader().getResourceAsStream("mjmq.properties");
        if (inputStream == null) {
//            LOGGER.warn("missing mjmq.properties");
            throw new RuntimeException("cant't find mjmq.properties");
        } else {
            try (InputStream in = new BufferedInputStream(inputStream)) {
                Properties p = new Properties();
                p.load(in);
                host = p.getProperty("host");
                if (host == null || !CommonUtils.isIp(host)) {
                    throw new RuntimeException("please set 'host' correctly in mjmq.properties");
                }
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
