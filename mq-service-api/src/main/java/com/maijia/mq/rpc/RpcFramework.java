package com.maijia.mq.rpc;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
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

    public static final ConcurrentHashMap<String, Object> REFERENCE_MAP = new ConcurrentHashMap<>();
    public static final Set SERVER_SOCKET_PORT_SET = Collections.synchronizedSet(new HashSet<Integer>());

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
            //为每个port指定一个ServerSocket来处理客户端的RPC调用请求
            Thread thread = new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();

                            //为每个请求分配一个线程来处理请求
                            new RPCRequestHandlerThread(socket).start();

                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    //执行到finally就说明该线程即将结束，SERVER_SOCKET_PORT_SET，以便之后可以再创建socketserver
                    SERVER_SOCKET_PORT_SET.remove(port);
                }
            }, "rpc-socketserver-port:" + port);
            thread.start();
        }

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

        //使用JDK[代理模式], 返回接口的代理
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, (proxy, method, arguments) -> {
            //notice:如果使用ObjectXXXStream，inputStream和outputStream按实际使用顺序进行获取，不能乱，否则会阻塞
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {

                objectOutputStream.writeUTF(interfaceClass.getName());
                objectOutputStream.writeUTF(method.getName());
                objectOutputStream.writeUTF(version);
                objectOutputStream.writeObject(method.getParameterTypes());
                objectOutputStream.writeObject(arguments);
                socket.shutdownOutput();

                Object result = objectInputStream.readObject();
                socket.shutdownInput();
                if (result instanceof Exception) {
                    throw (Exception) result;
                }
                return result;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
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