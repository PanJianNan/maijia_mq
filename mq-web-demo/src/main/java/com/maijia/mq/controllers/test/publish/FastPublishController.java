package com.maijia.mq.controllers.test.publish;

import com.maijia.mq.client.*;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PublishController
 *
 * @author panjn
 * @date 2016/12/28
 */
@RestController
@RequestMapping(value = "test/fast/publish")
public class FastPublishController {

    String queueName = "test.fast.publish1-1";
    String exchangeName = "fast.ex1";
//    String host = "192.168.102.137";
    String host = "127.0.0.1";

    @RequestMapping(value = "produce")
    public String produce(final String msg) throws IOException, InterruptedException {
        if (StringUtils.isBlank(msg)) {
            throw new IllegalArgumentException("msg is empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(3198);
        factory.setMode(FactoryMode.FAST);
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.DIRECT);

        for (int i = 0; i < 10; i++) {
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("msg", msg);
                    map.put("time", simpleDateFormat.format(new Date()) + " | fast | direct");
                    try {
                        channel.basicPublish(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
//            }, 0, 1000);
        }

        return "produce success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(final String msg) throws IOException, InterruptedException {
        if (StringUtils.isBlank(msg)) {
            throw new IllegalArgumentException("msg is empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(3198);
        factory.setMode(FactoryMode.FAST);
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.FANOUT);

        for (int i = 0; i < 10; i++) {
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("msg", msg);
                    map.put("time", simpleDateFormat.format(new Date()) + " | fast | fanout");
                    try {
                        channel.basicPublish(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                }
//            }, 0, 1000);
        }

        return "produce success";
    }

}
