package com.maijia.mq.rpc;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.util.HessianSerializeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * RpcServerReactor
 *
 * @author panjn
 * @date 2019/1/14
 */
public class RpcServerReactor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerReactor.class);

    private int port;

    public RpcServerReactor(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            //1. 打开多路复用器(选择器)
            selector = Selector.open();
            //2. 打开服务端通道
            serverSocketChannel = ServerSocketChannel.open();
            //3. 将服务端通道设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //4. 绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(port));
            //5. 将服务端通道注册到多路复用器(选择器)，并监听通道的accept事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new String("now can accept!"));
            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                //7. 遍历所以就绪的选择键（就绪事件）,做相应的处理
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    try {
                        if (selectionKey.isValid()) {
                            if (selectionKey.isAcceptable()) {
                                this.handleAccept(selectionKey);
                            } else if (selectionKey.isReadable()) {
                                this.handleRead(selectionKey);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        iterator.remove();
                        continue;
                    }

                    //8. 处理完毕，取消选择键 SelectionKey，避免重复处理
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            //执行到finally就说明该线程即将结束，SERVER_SOCKET_PORT_SET，以便之后可以再创建
            RpcFramework.SERVER_SOCKET_PORT_SET.remove(port);
            try {
                serverSocketChannel.close();
                serverSocketChannel.socket().close();
//                selector.selectNow();
                selector.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 处理接收连接就绪（accept）事件
     *
     * @param selectionKey
     */
    private void handleAccept(SelectionKey selectionKey) throws IOException {
//        LOGGER.debug(selectionKey.attachment());
        //我写的代码，所以我知道触发这个事件的channel是服务端的还是客户端的，NIO编程需谨慎，容易错，可以考虑用Netty简化编程
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        //1. 获得客户端通道，完成该操作才意味着完成TCP三次握手，TCP物理链路正式建立
        SocketChannel socketChannel = serverSocketChannel.accept();
        //2. 将客户端通道设置为非阻塞
        socketChannel.configureBlocking(false);
        //3. 将客户端通道注册到服务端的选择器上，并监听其读就绪事件
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, "now can read!");
    }

    /**
     * 处理读就绪事件
     *
     * @param selectionKey
     */
    private void handleRead(SelectionKey selectionKey) throws IOException {
//        LOGGER.debug(selectionKey.attachment());
        //我写的代码，所以我知道触发这个事件的channel是服务端的还是客户端的，NIO编程需谨慎，容易错，可以考虑用Netty简化编程
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byte[] totalBytes = new byte[1024];//存放read到的数据
        int readByteNum = 0;
        int totalReadByteNum = 0;//接收到的数据总字节数
        while ((readByteNum = socketChannel.read(byteBuffer)) > 0) {
            //如果存放read到的数据的byte数组容量不够，则对数组进行扩容
            if (readByteNum + totalReadByteNum > totalBytes.length) {
                byte[] newTotalBytes = new byte[totalBytes.length * 2];
                System.arraycopy(totalBytes, 0, newTotalBytes, 0, totalBytes.length);
                totalBytes = newTotalBytes;
            }

            byteBuffer.flip();//缓冲区由默认写状态切换为读状态
            byteBuffer.get(totalBytes, totalReadByteNum, readByteNum);
            totalReadByteNum += readByteNum;
//            byteBuffer.clear();//恢复默认写状态
        }

        if (readByteNum == -1) {
//            LOGGER.info(String.format("客户端可能关闭了:%s", JSONObject.toJSONString(socketChannel.getRemoteAddress())));
            //客户端channel可能已经正常关闭了，需要对该通道进行关闭和注销操作
            selectionKey.cancel();
            socketChannel.close();//todo 测试了下，不需要socketChannel.socket().close(), 关闭通道后socket自然就关闭了
            return;
        }

        //只取有效数据
        byte[] resultBytes = new byte[totalReadByteNum];
        System.arraycopy(totalBytes, 0, resultBytes, 0, totalReadByteNum);

        RpcRequestParam rpcRequestParam = HessianSerializeUtils.deserialize(resultBytes);
        LOGGER.debug("rpcRequestParam:" + JSONObject.toJSONString(rpcRequestParam));

        String interfaceName = rpcRequestParam.getInterfaceName();
        String methodName = rpcRequestParam.getMethodName();
        Class<?>[] parameterTypes = rpcRequestParam.getParameterTypes();
        Object[] arguments = rpcRequestParam.getArguments();
        String version = rpcRequestParam.getVersion();

        Object target = RpcFramework.REFERENCE_MAP.get(interfaceName + ":" + version);
        Object result;
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            result = method.invoke(target, arguments);
        } catch (NoSuchMethodException e) {
            LOGGER.error(e.getMessage(), e);
            result = e;
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
            result = e;
        } catch (InvocationTargetException e) {
            LOGGER.error(e.getMessage(), e);
            result = e;
        }

        //======返回RPC调用结果====
        byte[] data = HessianSerializeUtils.serialize(result);
        byteBuffer.clear();//恢复默认写状态
        byteBuffer.put(data);
        byteBuffer.flip();//缓冲区由默认写状态切换为读状态
        while (byteBuffer.hasRemaining()) {
            int len = socketChannel.write(byteBuffer);
            if (len < 0) {
                throw new EOFException();
            }
        }
        byteBuffer.clear();//恢复默认写状态

//        socketChannel.close();//todo 为什么要关闭通道？
//        socketChannel.socket().close();
    }
}
