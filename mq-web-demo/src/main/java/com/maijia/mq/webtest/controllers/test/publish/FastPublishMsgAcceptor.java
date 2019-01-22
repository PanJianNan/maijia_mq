package com.maijia.mq.webtest.controllers.test.publish;

import com.maijia.mq.client.*;
import com.maijia.mq.domain.Message;
import com.maijia.mq.service.MQConsumer;
import com.maijia.mq.service.impl.DefaultMQConsumer;
import com.maijia.mq.util.ConstantUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * FastPublishAcceptor
 *
 * @author panjn
 * @date 2016/12/29
 */
@Component
public class FastPublishMsgAcceptor extends AbstractMessageAcceptor {

    String queueName = "test.fast.publish1-1";
    String exchangeName = "fast.ex1";
//    String host = "192.168.102.137";
    String host = "127.0.0.1";

    @PostConstruct
    public void fire() {
        super.fire();
    }

    @Override
    protected void link() throws IOException, InterruptedException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(ConstantUtils.NIO_RPC_PORT);
        factory.setMode(FactoryMode.FAST);

        Connection connection = factory.newConnection();
        MqChannel channel = connection.createChannel();
        channel.queueDeclare(queueName);//注册需要消息队列名
        channel.setMqService(factory.getMqService());//尴尬
        channel.exchangeDeclare(exchangeName);

        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        MQConsumer consumer = new DefaultMQConsumer() {
            @Override
            public void handleDelivery(Message message) {
                System.out.println("[fast1-1] acceptor Received '" + message.getContent() + "'");
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
