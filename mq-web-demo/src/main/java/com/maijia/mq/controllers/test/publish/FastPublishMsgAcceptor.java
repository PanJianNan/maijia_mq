package com.maijia.mq.controllers.test.publish;

import com.maijia.mq.client.AbstractMessageAcceptor;
import com.maijia.mq.client.Channel;
import com.maijia.mq.client.Connection;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.IFastMqService;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * FastPublishAcceptor
 *
 * @author panjn
 * @date 2016/12/29
 */
//@Component
public class FastPublishMsgAcceptor extends AbstractMessageAcceptor {

//    @Resource
    private IFastMqService fastMqService;

    String queueName = "test.fast.publish2";
    String exchangeName = "fast.ex1";
    String host = "192.168.102.137";
//    String host = "127.0.0.1";

    @PostConstruct
    public void fire() {
        super.fire();
    }

    @Override
    protected void link() throws IOException {
        Connection connection = fastMqService.newConnection(host);
        Channel channel = connection.createChannel();
        channel.queueDeclare(queueName);
        channel.setMqService(fastMqService);
        channel.exchangeDeclare(exchangeName);

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer consumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Message message) {
                System.out.println("[fast2] acceptor Received '" + message.getContent() + "'");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        //自动回复队列应答 -- 消息确认机制
        channel.basicConsume(consumer);
    }

}
