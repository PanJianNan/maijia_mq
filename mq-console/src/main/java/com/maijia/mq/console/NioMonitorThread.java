package com.maijia.mq.console;

import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.consumer.LevelDBConsumer;
import com.maijia.mq.domain.Message;
import com.maijia.mq.producer.Producer;
import com.maijia.mq.util.ConstantUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NioMonitorThread
 *
 * @author panjn
 * @date 2017/4/24
 */
public class NioMonitorThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(NioMonitorThread.class);

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            //1. 获取通道
            ServerSocketChannel ssChannel = ServerSocketChannel.open();
            //2. 切换非阻塞模式
            ssChannel.configureBlocking(false);
            //3. 绑定连接
            ssChannel.bind(new InetSocketAddress(ConstantUtils.NIO_MSG_TRANSFER_PORT));
            //4. 获取选择器
            Selector selector = Selector.open();
            //5. 将通道注册到选择器上, 并且指定“监听接收事件”
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);
            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) {
                //7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey sk = null;
                    try {
                        //8. 获取准备“就绪”的是事件
                        sk = it.next();

                        //9. 判断具体是什么事件准备就绪
                        if (sk.isAcceptable()) {
                            //10. 若“接收就绪”，获取客户端连接
                            SocketChannel sChannel = ssChannel.accept();

                            //11. 切换非阻塞模式
                            sChannel.configureBlocking(false);

                            //12. 将该通道注册到选择器上
                            sChannel.register(selector, SelectionKey.OP_READ);
                        } else if (sk.isReadable()) {
                            //13. 获取当前选择器上“读就绪”状态的通道
                            SocketChannel sChannel = (SocketChannel) sk.channel();

                            //14. 读取数据
                            ByteBuffer buf = ByteBuffer.allocate(1024);

                            int len = 0;
                            while ((len = sChannel.read(buf)) > 0) {
                                buf.flip();
                                String recevice = new String(buf.array(), 0, len);
                                System.out.println("get request: " + recevice);
                                buf.clear();
                                //返回消息
                                Consumer levelDBConsumer = (Consumer) SpringContext.getBean(LevelDBConsumer.class);
                                Message message = levelDBConsumer.take(recevice);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(baos);
                                oos.writeObject(message);
                                ByteBuffer msgBuf = ByteBuffer.allocate(1024);
                                msgBuf.put(baos.toByteArray());
                                sChannel.write(msgBuf);
                            }
                        } /*else if () {
                            todo
                        }*/

                        //15. 取消选择键 SelectionKey
                        it.remove();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        it.remove();
                        sk.cancel();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
