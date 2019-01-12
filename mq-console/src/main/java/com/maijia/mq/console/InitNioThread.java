package com.maijia.mq.console;

import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.consumer.LevelDBConsumer;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 通过Spring初始化NIO线程
 * <p>
 * todo 在cosole包里有Spring的东西总感觉怪怪的，但是项目已web形式启动时，必须要启动NIO的监听线程
 *
 * @author panjn
 * @date 2017/1/3
 */
@Component
public class InitNioThread {

    @Resource
    private ApplicationContextHelper applicationContextHelper;

    private static final Logger LOGGER = Logger.getLogger(InitNioThread.class);

    @PostConstruct
    public void init() throws Exception {
        LOGGER.info("start init NioMonitorThread");
        Consumer consumer = applicationContextHelper.getBean(LevelDBConsumer.class);//todo 这里使用LevelDBConsumer来测试
        NioMonitorThread nioMonitorThread = new NioMonitorThread(consumer);
        nioMonitorThread.setName("NioMonitorThread");
        nioMonitorThread.start();
        LOGGER.info("end init NioMonitorThread");
    }

}
