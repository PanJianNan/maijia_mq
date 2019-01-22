package com.maijia.mq.webtest.controllers.test.publish;

import com.maijia.mq.client.*;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;
import com.maijia.mq.util.ConstantUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    public String produce2(final String msg, Integer sec) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg can't be empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(ConstantUtils.NIO_RPC_PORT);
        factory.setMode(FactoryMode.FILE);
        Connection connection = factory.newConnection();
        final MqChannel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.DIRECT);

        Random random = new Random();
        Timer[] timer = new Timer[10];
        for (int i = 0; i < 10; i++) {
            timer[i] = new Timer();
            timer[i].schedule(new TimerTask() {
                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("msg", msg + random.nextInt(100));
                    map.put("time", simpleDateFormat.format(new Date()) + " | file | direct");
                    try {
                        channel.basicPublish(map);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 50);
        }

        if (sec == null) {
            sec = 60;
        }
        TimeUnit.SECONDS.sleep(sec);
        if (sec != -1) {
            Arrays.stream(timer).forEach(t -> t.cancel());
        }

        return "produce success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(String msg, Integer sec) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg can't be empty");
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(ConstantUtils.NIO_RPC_PORT);
        factory.setMode(FactoryMode.FILE);
        Connection connection = factory.newConnection();
        final MqChannel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.exchangeDeclare(exchangeName, ExchangeType.FANOUT);//如果是对分组进行广播，则不需要队列名

        Random random = new Random();
        Timer[] timer = new Timer[10];
        for (int i = 0; i < 10; i++) {
            timer[i] = new Timer();
            timer[i].schedule(new TimerTask() {
                @Override
                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("msg", msg + random.nextInt(100));
                    map.put("time", simpleDateFormat.format(new Date()) + " | file | broadcast");
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

        if (sec == null) {
            sec = 60;
        }
        TimeUnit.SECONDS.sleep(sec);
        if (sec != -1) {
            Arrays.stream(timer).forEach(t -> t.cancel());
        }

        return "produce success";
    }

    @RequestMapping(value = "getMessage")
    public String getMessageOnce(String queueName, Boolean loop) throws IOException, InterruptedException {
        if (queueName == null) {
            throw new IllegalArgumentException("queueName can't be empty");
        }
        if (loop == null) {
            loop = false;
        }
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(ConstantUtils.NIO_RPC_PORT);
        factory.setMode(FactoryMode.FILE);
        Connection connection = factory.newConnection();
        final MqChannel channel = connection.createChannel();
        channel.setMqService(factory.getMqService());
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.DIRECT);
        channel.setLoopRequest(loop);//就请求一次 or 循环请求

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer mqConsumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Message message) {
                if (message == null) {
                    System.err.println("once[file1-1] acceptor Received [null], what fuk");
                } else {
                    System.out.println("once[file1-1] acceptor Received '" + message.getContent() + "'");
                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        };

        //自动回复队列应答 -- 消息确认机制
        channel.basicConsume(mqConsumer);
        return "consume success";
    }

}
