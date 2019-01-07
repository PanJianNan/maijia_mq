package com.maijia.mq.webtest.controllers.test.publish;

import com.maijia.mq.client.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PublishController
 *
 * @author panjn
 * @date 2016/12/28
 */
@RestController
@RequestMapping(value = "test/file/publish")
public class FilePublishController {

    String queueName = "test.file.publish1-1";
    String exchangeName = "file.ex1";
    String host = "127.0.0.1";

    @RequestMapping(value = "direct")
    public String produce(final String msg) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg is empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(3198);
        factory.setMode(FactoryMode.FILE);
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.DIRECT);
        for (int i = 0; i < 10; i++) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("time", simpleDateFormat.format(new Date()) + "_direct");
                    try {
                        channel.basicPublish(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000);
        }

        return "produce success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(String msg) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg is empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(3198);
        factory.setMode(FactoryMode.FILE);
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.exchangeDeclare(exchangeName, ExchangeType.FANOUT);
        for (int i = 0; i < 10; i++) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("time", simpleDateFormat.format(new Date()) + "_broadcast");
                    try {
                        channel.basicPublish(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000);
        }
        return "produce success";
    }

}
