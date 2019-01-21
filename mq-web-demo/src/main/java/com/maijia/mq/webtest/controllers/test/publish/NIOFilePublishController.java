package com.maijia.mq.webtest.controllers.test.publish;

import com.maijia.mq.client.*;
import com.maijia.mq.domain.Message;
import com.maijia.mq.util.ConstantUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
                    map.put("time", simpleDateFormat.format(new Date()) + " | direct_nio");
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
        boolean isRun = true;

        if (StringUtils.isBlank(queueName)) {
            queueName = this.queueName;
//            throw new RuntimeException("queueName is empty");
        }

        //1. 打开选择器（多路复用器）
        Selector selector = Selector.open();
        //2. 打开通道
        SocketChannel sChannel = SocketChannel.open();
        //3. 设置通道非阻塞模式
        sChannel.configureBlocking(false);
        //4. 创建连接
        sChannel.connect(new InetSocketAddress(host, ConstantUtils.NIO_MSG_TRANSFER_PORT));
        //5. 将通道注册到选择器上
        sChannel.register(selector, SelectionKey.OP_CONNECT);

        int selectNum;
//        while ((selectNum = selector.select()) > 0) {
        while (isRun) {
            selectNum = selector.select();

            //此方法执行处于阻塞模式的选择操作
            System.out.println("selectNum:" + selectNum);
            if (selectNum <= 0) {
                break;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey sk = iterator.next();
                SocketChannel socketChannel = (SocketChannel) sk.channel();

                System.out.println("sk isValid:" + sk.isValid());

                if (sk.isValid()) {
                    if (sk.isConnectable()) {
                        if (socketChannel.isConnectionPending()) {
                            socketChannel.finishConnect();//等待连接建立
                            //6.连接建立后向服务端发送队列名queueName
                            ByteBuffer buf = ByteBuffer.allocate(1024);
                            buf.put(queueName.getBytes());
                            buf.flip();//缓冲区由写状态切换为读状态
                            System.out.println("ops=" + sk.interestOps());
//                        sChannel.register(selector, SelectionKey.OP_READ);//待服务端传回消息触发事件
                            sk.interestOps(SelectionKey.OP_READ);//待服务端传回消息触发事件
                            //connect|read后 read之后的下一次selector.select()居然没有阻塞且返回0 ？？？ 但是可以变相的退出wile(true)循环
//                        sk.interestOps(sk.interestOps() | SelectionKey.OP_READ);
//                            socketChannel.write(buf);
                            //操作系统发送缓冲区可能会满，导致无限循环，这样最终会导致CPU利用率100%。 todo 解决可以参考Netty的方式，进行一定的阻塞等待
                            while (buf.hasRemaining()) {
                                int len = socketChannel.write(buf);
                                if (len < 0) {
                                    throw new EOFException();
                                }
                            }

                            System.out.println("ops=" + sk.interestOps());
                        }
                    } else if (sk.isReadable()) {
                        //7.读取服务端返回的消息内容
                        ByteBuffer buf = ByteBuffer.allocate(1024);
                        int readByteNum;
                        while ((readByteNum = socketChannel.read(buf)) > 0) {//假设消息都小于1024字节，todo 待改善
                            buf.flip();//缓冲区由写状态切换为读状态
                            byte[] bytes = new byte[readByteNum];
                            buf.get(bytes, 0, bytes.length);
                            try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                                Message msg = (Message) objectInputStream.readObject();
                                System.out.println(msg);
                                System.out.println("receive msg:" + msg.getContent());
                                sk.cancel();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //消息接收完毕
                        isRun = false;
                    } /*else if (sk.isWritable()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    byteBuffer.put(queueName.getBytes());
                    byteBuffer.flip();
                    int len = socketChannel.write(byteBuffer);
                    System.out.println(byteBuffer.array());
                    System.out.println(len);
                    sk.cancel();
                }*/


                    //8.取消选择键 SelectionKey
                    iterator.remove();
                }
            }

        }

        //5. 关闭通道
        sChannel.close();
        sChannel.socket().close();
        selector.close();

        System.out.println("@@@ end !!! @@@");



  /*      //3. 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

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
        sChannel.close();*/

        return "consume success";
    }

    @RequestMapping(value = "broadcast")
    public String broadcast(String msg) throws IOException, InterruptedException {
        return "produce success";
    }

}
