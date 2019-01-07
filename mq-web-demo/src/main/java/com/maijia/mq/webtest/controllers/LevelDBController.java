/*
package com.maijia.mq.webtest.controllers;

import com.maijia.mq.consumer.LevelDBConsumer;
import com.maijia.mq.producer.LevelDBProducer;
import com.maijia.mq.leveldb.other.EventSourceBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Random;

*/
/**
 * LevelDBController
 *
 * @author panjn
 * @date 2016/12/15
 *//*

@RestController
@RequestMapping(value = "leveldb")
public class LevelDBController {

    @Resource
    private LevelDBProducer producer;
    @Resource
    private LevelDBConsumer consumer;

*/
/*    @RequestMapping(value = "init")
    public String init() throws Exception {
        producer.init("pjn123", new String("haha"));
        return "success";
    }*//*


    @RequestMapping(value = "produce")
    public String produce(String queueName, String msg) throws Exception {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }
        if (msg == null) {
            throw new IllegalArgumentException("消息不能为空！");
        }
        Random random = new Random();
        System.out.println("=开生" + queueName);
//        producer.produce("pjn", random.nextInt(100) + 1000);
        producer.produce(queueName, msg);
        System.out.println("生完=" + queueName);
        return "success";
    }

    @RequestMapping(value = "consume")
    public String consume(String queueName) throws Exception {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("队列名称不能为空！");
        }
        System.out.println("开始消费" + queueName);
        EventSourceBase evt  = (EventSourceBase) consumer.take(queueName);
        System.out.println(evt.getEventName() + " @" + queueName);
        System.out.println("结束消费" + queueName);
        return evt.getEventName();
    }
}
*/
