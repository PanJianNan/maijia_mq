package com.maijia.mq.controllers.test.publish;

import com.maijia.mq.client.*;
import com.maijia.mq.domain.Message;
import com.maijia.mq.util.ConstantUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PublishController
 *
 * @author panjn
 * @date 2016/12/28
 */
@RestController
@RequestMapping(value = "test/file/publish/nio")
public class NIOFilePublishController {

    String queueName = "test.file.publish.nio.1-1";
    String exchangeName = "file.ex1";
    String host = "127.0.0.1";

    @RequestMapping(value = "produce")
    public String produce(String msg) throws IOException, InterruptedException {
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
//            Timer timer = new Timer();
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
                    Map<String, Object> map = new HashMap<>();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("msg", msg);
                    map.put("time", simpleDateFormat.format(new Date()) + "_direct_nio");
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

    @RequestMapping(value = "consume")
    public String consume(String queueName) throws IOException, InterruptedException {
        if (StringUtils.isBlank(queueName)) {
            queueName = this.queueName;
//            throw new RuntimeException("queueName is empty");
        }

        //1. 获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress(host, ConstantUtils.NIO_MSG_TRANSFER_PORT));

        //2. 切换非阻塞模式
        sChannel.configureBlocking(true);

        //3. 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

//        //4. 发送数据给服务端
//        Scanner scan = new Scanner(System.in);
//
//        while(scan.hasNext()){
//            String str = scan.next();
//            if ("-1".equals(str)) {
//                break;
//            }
//            buf.put((new Date().toString() + "\n" + str).getBytes());
//            buf.flip();
//            sChannel.write(buf);
//            buf.clear();
//        }

        //4. 发送数据给服务端
        buf.put(queueName.getBytes());
        buf.flip();
        sChannel.write(buf);
        System.out.println("isConnectionPending:" + sChannel.isConnectionPending());
        System.out.println("isConnected:" + sChannel.isConnected());

        ByteBuffer receiveBuf = ByteBuffer.allocate(1024);
        sChannel.read(receiveBuf);
        receiveBuf.flip();

        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(receiveBuf.array()))) {
            Message msg = (Message) objectInputStream.readObject();
            System.out.println(msg);
            System.out.println("receive msg:" + msg.getContent());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        //5. 关闭通道
        sChannel.close();

        return "consume success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(String msg) throws IOException, InterruptedException {
        return "produce success";
    }

}
