package com.maijia.mq.controllers.test.publish;

import com.alibaba.dubbo.rpc.RpcException;
import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.client.ExchangeType;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * PublishController
 *
 * @author panjn
 * @date 2016/12/28
 */
@RestController
@RequestMapping(value = "test/fast/publish")
public class FastPublishController {
    @Resource
    private IFastMqService fastMqService;

    String queueName = "test.fast.publish1-1";
    String exchangeName = "fast.ex1";
    String host = "127.0.0.1";

    @RequestMapping(value = "produce")
    public String produce(String msg) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg is empty");
        }
        Channel channel = new Channel();
        channel.queueDeclare(queueName);
        channel.exchangeDeclare(exchangeName, ExchangeType.DIRECT);
        channel.setMqService(fastMqService);
        channel.basicPublish(msg);
        return "produce success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(String msg) throws IOException, InterruptedException {
        if (msg == null) {
            throw new IllegalArgumentException("msg is empty");
        }
        Channel channel = new Channel();
        channel.queueDeclare(queueName);
        channel.setMqService(fastMqService);
        channel.exchangeDeclare(exchangeName, ExchangeType.FANOUT);
        channel.basicPublish(msg);
        return "produce success";
    }

    @RequestMapping(value = "socket")
    public String socketTest() throws IOException {
        Connection connection = fastMqService.newConnection(host);
        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName);
        channel.setMqService(fastMqService);
        channel.exchangeDeclare(exchangeName);

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer consumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Message message) {
                System.out.println("[fast1-1] controller Received '" + message.getContent() + "'");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        //自动回复队列应答 -- 消息确认机制
        channel.basicConsume(consumer);

        //如果执行到这说明连接已经断开
        return "消费异常，断开连接";
    }

    @RequestMapping(value = "socket2")
    public String socketTest2() throws IOException {
        consume2();
        retryLink2();
        return "消费异常，断开连接";
    }

    private boolean consume2() throws IOException {
        String QUEUE_NAME = "test.fast.publish1-2";
        Connection connection = fastMqService.newConnection(host);
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME);
        channel.setMqService(fastMqService);
        channel.exchangeDeclare(exchangeName);

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer consumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Message message) {
                System.out.println("[fast1-2] controller Received '" + message.getContent() + "'");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        //自动回复队列应答 -- 消息确认机制
        channel.basicConsume(consumer);

        return false;
    }

    private void retryLink2() throws IOException {
        try  {
            System.out.println("===========尝试重连MJMQ==========");
            consume2();
        } catch (RpcException e) {
            System.out.println("===========重连MJMQ失败，1分后重试！==========");
            try {
                Thread.sleep(60 * 1000);
                retryLink2();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
