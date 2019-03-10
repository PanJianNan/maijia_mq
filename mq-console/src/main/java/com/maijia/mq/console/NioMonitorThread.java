package com.maijia.mq.console;

import com.maijia.mq.constant.CommonConstant;
import com.maijia.mq.consumer.Consumer;
import com.maijia.mq.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(NioMonitorThread.class);

    private Consumer consumer;

    public NioMonitorThread(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void run() {
        Selector selector = null;
        ServerSocketChannel ssChannel = null;
        try {
            //1. 打开选择器(多路复用器)
            selector = Selector.open();
            //2. 打开服务器通道
            ssChannel = ServerSocketChannel.open();
            //3. 设置服务器通道为非阻塞模式
            ssChannel.configureBlocking(false);
            //4. 绑定监听端口
            ssChannel.bind(new InetSocketAddress(CommonConstant.NIO_MSG_TRANSFER_PORT));
            //5. 将通道注册到选择器上, 并且指定“监听接收事件”
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);
            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (true) {
                //7.让多路复用器开始监听，阻塞,只有当至少一个注册的事件发生的时候才会继续.
                int selectNum = selector.select();
//                selector.select(1000);//无论是否有事件发生，selector每隔1s被唤醒一次
                System.out.println("selectNum:" + selectNum);
                if (selectNum <= 0) {
                    continue;
                }

                //8. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    SelectionKey sk = null;
                    try {
                        //9. 获取准备“就绪”的事件
                        sk = it.next();

                        System.out.println("sk isValid:" + sk.isValid());

                        if (sk.isValid()) {
                            //10. 判断具体是什么事件准备就绪
                            if (sk.isAcceptable()) {
                                //11. 若“接收就绪”，获取客户端连接
                                //通过ServerSocketChannel的accept创建SocketChannel实例
                                //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
                                SocketChannel sChannel = ssChannel.accept();

                                //12. 将客户端的Channel切换成非阻塞模式
                                sChannel.configureBlocking(false);

                                //13. 将客户端通道注册到选择器上，选择器监听器其"读就绪"事件
                                sChannel.register(selector, SelectionKey.OP_READ);
                            } else if (sk.isReadable()) {
                                //14. 获取当前选择器上“读就绪”状态的通道
                                SocketChannel sChannel = (SocketChannel) sk.channel();

                                //15. 读取数据
                                ByteBuffer buf = ByteBuffer.allocate(1024);

                                int readByteNum = 0;
                                while ((readByteNum = sChannel.read(buf)) > 0) {
                                    //将缓冲区当前的limit设置为position=0，用于后续对缓冲区的读取操作
                                    buf.flip();//缓冲区由写状态切换为读状态
                                    String recevice = new String(buf.array(), 0, readByteNum);
                                    System.out.println("get request: " + recevice);
                                    buf.clear();//恢复默认写状态

                                    //返回消息
                                    Message message = consumer.take(recevice);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                                    oos.writeObject(message);
                                    buf.put(baos.toByteArray());
                                    buf.flip();//缓冲区由写状态切换为读状态
                                    //操作系统发送缓冲区可能会满，导致无限循环，这样最终会导致CPU利用率100%。 todo 解决可以参考Netty的方式，进行一定的阻塞等待
                                    while (buf.hasRemaining()) {
                                        int len = sChannel.write(buf);
                                        if (len < 0) {
                                            throw new EOFException();
                                        }
                                    }

                                }

                                if (readByteNum == -1) {
                                    //客户端channel已经正常关闭了，需要对该通道进行关闭和注销操作
                                    sk.cancel();
                                    sChannel.close();
                                    System.out.println("客户端channel已经正常关闭");
                                }
                            }
                        } /*else if () {
                            todo
                        }*/

                        //16. 取消选择键 SelectionKey
                        it.remove();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        it.remove();
                        sk.cancel();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                ssChannel.close();
                ssChannel.socket().close();
                selector.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

        }
    }
}
