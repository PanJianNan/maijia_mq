package com.maijia.mq.client;

import com.maijia.mq.service.MQConsumer;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Connection
 *
 * @author panjn
 * @date 2016/10/26
 */
public class Connection implements Serializable {

    private static final long serialVersionUID = -270496099988459289L;

    private static final Logger LOGGER = Logger.getLogger(Connection.class);

    private String host = "localhost";
    private int port = -1;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * new channel
     */
    public Channel createChannel() {
        return new Channel(host, port);
    }

    /**
     * 消费消息，采用ACK确认机制
     *
     * @param queueName
     * @param consumer
     */
    public void basicConsume(String queueName, MQConsumer consumer) {
        ObjectInputStream is = null;
        ObjectOutputStream os = null;
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            //由系统标准输入设备构造BufferedReader对象
            os = new ObjectOutputStream(socket.getOutputStream());
            //由Socket对象得到输出流，并构造PrintWriter对象
            is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            while (true) {
                //请求消息 (写)
                os.writeObject(queueName);
                os.flush();
                System.out.println("发起消费请求");

                //获得消息 （读）
                Object obj = is.readObject();
                //进行消费
                consumer.handleDelivery(obj);

                //确认消费成功 （写）
                os.writeObject("success");
                System.out.println("确认消费成功");

//                Thread.sleep(3000);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                os.close(); //关闭Socket输出流
                is.close(); //关闭Socket输入流
                socket.close(); //关闭Socket
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void close() {
        //todo
    }
}
