package com.maijia.mq.console;

import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.consumer.DefaultConsumer;
import com.maijia.mq.consumer.LevelDBConsumer;
import com.maijia.mq.consumer.RedisConsumer;
import com.maijia.mq.producer.DefaultProducer;
import com.maijia.mq.producer.LevelDBProducer;
import com.maijia.mq.producer.RedisProducer;
import com.maijia.mq.constant.CommonConstant;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 通过Spring初始化NIO线程
 * <p>
 * todo 在console包里有Spring的东西总感觉怪怪的，但是项目已web形式启动时，必须要启动NIO的监听线程
 *
 * @author panjn
 * @date 2017/1/3
 */
@Component
public class InitNioThread {

    private static final Logger LOGGER = Logger.getLogger(InitNioThread.class);

    @Resource
    private ApplicationContextHelper applicationContextHelper;
    @Resource
    private DefaultConsumer defaultConsumer;
    @Resource
    private DefaultProducer defaultProducer;
    @Resource
    private RedisConsumer redisConsumer;
    @Resource
    private RedisProducer redisProducer;
    @Resource
    private LevelDBConsumer levelDBConsumer;
    @Resource
    private LevelDBProducer levelDBProducer;


    @PostConstruct
    public void init() throws Exception {
        LOGGER.info("start init NioMonitorThread");
        Consumer consumer = applicationContextHelper.getBean(LevelDBConsumer.class);//todo 这里使用LevelDBConsumer来测试
        NioMonitorThread nioMonitorThread = new NioMonitorThread(consumer);
        nioMonitorThread.setName("NioMonitorThread");
        nioMonitorThread.start();
        LOGGER.info("end init NioMonitorThread");

        LOGGER.info("start init MqListenThread");
        this.initMqListenThread();
        LOGGER.info("end init MqListenThread");

    }

    private void initMqListenThread() {
        Thread fastMqListenThread = new Thread(new MqServerThread(CommonConstant.FAST_MQ_LISTEN_PORT, defaultConsumer, defaultProducer), "MqServerThread-FAST");
        Thread cacheMqListenThread = new Thread(new MqServerThread(CommonConstant.CACHE_MQ_LISTEN_PORT, redisConsumer, redisProducer), "MqServerThread-CACHE");
        Thread fileMqListenThread = new Thread(new MqServerThread(CommonConstant.FILE_MQ_LISTEN_PORT, levelDBConsumer, levelDBProducer), "MqServerThread-FILE");

        fastMqListenThread.start();
        cacheMqListenThread.start();
        fileMqListenThread.start();
    }

}
