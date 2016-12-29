package com.maijia.mq.controllers.test.publish;

import com.alibaba.dubbo.rpc.RpcException;
import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * FastPublishAcceptor
 *
 * @author panjn
 * @date 2016/12/29
 */
@Component
public class FastPublishAcceptor {

    @Resource
    private IFastMqService fastMqService;

    String queueName = "test.fast.publish1";
    String exchangeName = "fast.ex1";
    String host = "127.0.0.1";

    @PostConstruct
    public void socketTest2() throws IOException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("init message acceptor");
                    consume2();
                    retryLink2();
                    System.out.println("消费异常，断开连接");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private boolean consume2() throws IOException {
        String QUEUE_NAME = "test.fast.publish2";
        Connection connection = fastMqService.newConnection(host);
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME);
        channel.setMqService(fastMqService);
        channel.exchangeDeclare(exchangeName);

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer consumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Object message) {
                System.out.println("C2 [x] Received '" + message + "'");
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
