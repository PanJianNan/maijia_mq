package com.maijia.mq.rpc;

import org.apache.log4j.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple RPC framework
 *
 * @author panjn
 * @date 2017/1/2
 */
public class RpcFramework {

    private static final Logger LOGGER = Logger.getLogger(RpcFramework.class);

    public static final Map<String, Object> REFERENCE_MAP = new HashMap<>();
    public static final Map<Integer, ServerSocket> SERVER_SOCKET_MAP = new HashMap<>();

    /**
     * 暴露服务
     *
     * @param service 服务实现
     * @param port    服务端口
     * @throws Exception
     */
    public synchronized static void export(Class interfaceClass, final Object service, int port) throws Exception {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("interfaceClass is null");
        }
        if (service == null) {
            throw new IllegalArgumentException("service is null");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port %d ", port));
        }
        LOGGER.info(String.format("Export service %s on port %d", service.getClass().getName(), port));
        //save reference map
        REFERENCE_MAP.put(interfaceClass.getName(), service);

        if (SERVER_SOCKET_MAP.get(port) != null) {
            return;
        }

        final ServerSocket serverSocket = new ServerSocket(port);
        SERVER_SOCKET_MAP.put(port, serverSocket);

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();

                    new Thread(() -> {
                        try {
                            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                                 ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {

                                String interfaceName = input.readUTF();
                                String methodName = input.readUTF();
                                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                                Object[] arguments = (Object[]) input.readObject();

                                try {
                                    Object target = REFERENCE_MAP.get(interfaceName);
                                    Method method = target.getClass().getMethod(methodName, parameterTypes);
                                    Object result = method.invoke(target, arguments);
                                    output.writeObject(result);
                                } catch (Throwable t) {
                                    output.writeObject(t);
                                }
                            } finally {
                                socket.close();
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }).start();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });
        thread.start();

    }

    /**
     * 引用服务
     *
     * @param <T>            接口泛型
     * @param interfaceClass 接口类型
     * @param host           服务器主机名
     * @param port           服务器端口
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class == null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(String.format("The %s must be interface class!", interfaceClass.getName()));
        }
        if (host == null || host.length() == 0) {
            throw new IllegalArgumentException("Host == null!");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port %d", port));
        }
        LOGGER.info(String.format("Get remote service %s from server %s:%i", interfaceClass.getName(), host, port));
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, (proxy, method, arguments) -> {
            Socket socket = new Socket(host, port);
            try {
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                try {
                    output.writeUTF(interfaceClass.getName());
                    output.writeUTF(method.getName());
                    output.writeObject(method.getParameterTypes());
                    output.writeObject(arguments);
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    try {
                        Object result = input.readObject();
                        if (result instanceof Throwable) {
                            throw (Throwable) result;
                        }
                        return result;
                    } finally {
                        input.close();
                    }
                } finally {
                    output.close();
                }
            } finally {
                socket.close();
            }
        });
    }

}
