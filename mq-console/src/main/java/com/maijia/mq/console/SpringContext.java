package com.maijia.mq.console;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.Properties;

/**
 * spring容器初始化类，并提供从容器中获取对象的方法
 *
 * @author panjn
 * @date 2017/1/11
 */
public class SpringContext {

    private static final Logger LOGGER = Logger.getLogger(ServerManager.class);

    private static ApplicationContext applicationContext;

    /**
     * 初始化spring上下文
     */
    public static void initSpringContext() {

        String[] configs = {"classpath*:spring/spring-application-context.xml", "classpath*:spring/spring-cache-default.xml"};

        //加载配置文件
        InputStream inputStream = SpringContext.class.getClassLoader().getResourceAsStream("conf" + File.separator + "mjmq-server.properties");
        if (inputStream == null) {
            LOGGER.warn("missing mjmq-server.properties");
        } else {
            try (InputStream in = new BufferedInputStream(inputStream)) {
                Properties p = new Properties();
                p.load(in);

                String useredis = p.getProperty("useredis");
                if ("true".equals(useredis)) {
                    configs = new String[]{"classpath*:spring/spring-application-context.xml", "classpath*:spring/spring-redis.xml"};
                }
            } catch (FileNotFoundException e) {
                LOGGER.warn("missing mjmq-server.properties");
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage(), e);
            }
        }

        applicationContext = new ClassPathXmlApplicationContext(configs);
//        applicationContext = new FileSystemXmlApplicationContext(configs);
    }

    /**
     * 获取spring 容器里面的bean
     *
     * @param beanName
     * @return Object
     */
    public static Object getBean(String beanName) {
        try {
            return applicationContext.getBean(beanName);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * 获取spring 容器里面的bean
     *
     * @param clazz
     * @return Object
     */
    public static Object getBean(Class<?> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }
}
