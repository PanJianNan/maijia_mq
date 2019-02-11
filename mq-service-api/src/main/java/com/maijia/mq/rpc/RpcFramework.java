package com.maijia.mq.rpc;

import com.alibaba.fastjson.JSONObject;
import com.maijia.mq.constant.CommonConstant;
import com.maijia.mq.util.HessianSerializeUtils;
import org.apache.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple RPC framework
 *
 * @author panjn
 * @date 2017/1/2
 */
public class RpcFramework {

    private static final Logger LOGGER = Logger.getLogger(RpcFramework.class);

    public static final String DEFAULT_VERSION = "1.0.0";

    public static final ConcurrentHashMap<String, Object> REFERENCE_MAP = new ConcurrentHashMap<>();
    public static final Set SERVER_SOCKET_PORT_SET = Collections.synchronizedSet(new HashSet<Integer>());

    /**
     * 暴露服务
     *
     * @param interfaceClass 接口类型
     * @param serviceImpl    服务实现
     * @param version        接口版本
     * @throws Exception
     */
    public static void export(Class interfaceClass, Object serviceImpl, String version) throws Exception {
        export(interfaceClass, serviceImpl, CommonConstant.NIO_RPC_PORT, version);
    }

    /**
     * 暴露服务
     *
     * @param interfaceClass 接口类型
     * @param serviceImpl    服务实现
     * @param port           服务端口
     * @param version        接口版本
     * @throws Exception
     */
    public static void export(Class interfaceClass, Object serviceImpl, int port, String version) throws Exception {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("interfaceClass can't be null");
        }
        if (serviceImpl == null) {
            throw new IllegalArgumentException("serviceImpl can't be null");
        }
        if (serviceImpl.getClass().isInterface()) {
            throw new IllegalArgumentException("serviceImpl must not be interface");
        }
        if (!interfaceClass.isInstance(serviceImpl)) {
            throw new IllegalArgumentException("serviceImpl must implements interfaceClass");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port %d ", port));
        }
        LOGGER.info(String.format("Start export service %s(version:%s) on port %d", serviceImpl.getClass().getName(), version, port));
        //save reference map
        REFERENCE_MAP.putIfAbsent(interfaceClass.getName() + ":" + version, serviceImpl);//todo 如果重复暴露相同接口，需要什么特殊处理么？


        if (SERVER_SOCKET_PORT_SET.contains(port)) {
            return;
        }

        if (SERVER_SOCKET_PORT_SET.add(port)) {
            Thread thread = new Thread(new RpcServerReactor(port), "rpc-nio-server-port:" + port);
            thread.start();
        }

    }

    /**
     * 引用服务
     *
     * @param <T>            接口泛型
     * @param interfaceClass 接口类型
     * @param host           服务器主机名
     * @param version        接口版本
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host,  final String version) {
       return refer(interfaceClass, host, CommonConstant.NIO_RPC_PORT, version);
    }

    /**
     * 引用服务
     *
     * @param <T>            接口泛型
     * @param interfaceClass 接口类型
     * @param host           服务器主机名
     * @param port           服务器端口
     * @param version        接口版本
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port, final String version) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class can't be null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(String.format("The %s must be interface class!", interfaceClass.getName()));
        }
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host can't be null!");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port %d", port));
        }
        LOGGER.info(String.format("Start get remote service %s(version:%s) from server %s:%d", interfaceClass.getName(), version, host, port));

        // todo 消费方注册到zk上
        // zk.client();


        //使用JDK[代理模式], 返回接口的代理
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, (proxy, method, arguments) -> {

            boolean isRun = true;
            Object result = null;

            //1. 打开多路复用器（选择器）
            Selector selector = Selector.open();
            //2. 打开客户端通道
            SocketChannel socketChannel = SocketChannel.open();
            //3. 设置通道为非阻塞
            socketChannel.configureBlocking(false);
            //4. socketChannel.
            if(!socketChannel.connect(new InetSocketAddress(host, port))) {
                //5. 将客户端通道注册到多路复用器(选择器)，并监听通道的connect事件
                socketChannel.register(selector, SelectionKey.OP_CONNECT, "now can connect!");
            }

            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            try {
                while (isRun) {
                    //此方法执行处于阻塞模式的选择操作
                    if (selector.select() <= 0) {
                        break;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isValid()) {
                            if (selectionKey.isConnectable()) {
                                LOGGER.debug(selectionKey.attachment());
                                if (socketChannel.isConnectionPending()) {
                                    //等待连接建立
                                    socketChannel.finishConnect();

                                    selectionKey.interestOps(SelectionKey.OP_READ);//待服务端传回消息触发事件
                                    selectionKey.attach("now can read!");

                                    RpcRequestParam rpcRequestParam = new RpcRequestParam();
                                    rpcRequestParam.setInterfaceName(interfaceClass.getName());
                                    rpcRequestParam.setMethodName(method.getName());
                                    rpcRequestParam.setParameterTypes(method.getParameterTypes());
                                    rpcRequestParam.setArguments(arguments);
                                    rpcRequestParam.setVersion(version);
                                    byte[] data = HessianSerializeUtils.serialize(rpcRequestParam);

                                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                    byteBuffer.put(data);
                                    byteBuffer.flip();//缓冲区由写状态切换为读状态
                                    while (byteBuffer.hasRemaining()) {
                                        int len = socketChannel.write(byteBuffer);
                                        if (len < 0) {
                                            throw new EOFException();
                                        }
                                    }
                                }
                            } else if (selectionKey.isReadable()) {
                                LOGGER.debug(selectionKey.attachment());
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
                                    byteBuffer.clear();//恢复默认写状态
                                }

                                if (readByteNum == -1) {
                                    LOGGER.info(String.format("服务端可能关闭了:%s", JSONObject.toJSONString(socketChannel.getRemoteAddress())));
                                    //客户端channel可能已经正常关闭了，需要对该通道进行关闭和注销操作
                                    selectionKey.channel();
                                    socketChannel.close();
                                    throw new RuntimeException("服务端可能关闭了");
                                }

                                //只取有效数据
                                byte[] resultBytes = new byte[totalReadByteNum];
                                System.arraycopy(totalBytes, 0, resultBytes, 0, totalReadByteNum);

                                result = HessianSerializeUtils.deserialize(resultBytes);
                                LOGGER.debug("result:" + JSONObject.toJSONString(result));

                                //接收完毕
                                isRun = false;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                //关闭通道
                try {
                    socketChannel.socket().close();//todo 测试了下，这一步多余，不需要socketChannel.socket().close(), 关闭通道( socketChannel.close())后socket自然就关闭了
                    socketChannel.close();
//                    selector.selectNow();//https://blog.csdn.net/pange1991/article/details/86491520
                    selector.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }


            return result;
        });
    }


    /**
     * 处理RPC请求的线程
     */
    private static class RPCRequestHandlerThread extends Thread {

        private Socket socket;

        public RPCRequestHandlerThread(Socket socket) {
            this.socket = socket;
            this.setName("RPCRequestHandlerThread-port:" + socket.getPort());
        }

        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

                String interfaceName = objectInputStream.readUTF();
                String methodName = objectInputStream.readUTF();
                String version = objectInputStream.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) objectInputStream.readObject();
                Object[] arguments = (Object[]) objectInputStream.readObject();

                Object target = REFERENCE_MAP.get(interfaceName + ":" + version);

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
                } finally {
                    socket.shutdownInput();
                }

                objectOutputStream.writeObject(result);
                socket.shutdownOutput();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }


}