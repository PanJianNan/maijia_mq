package com.maijia.mq.client;

import com.maijia.mq.domain.Message;
import com.maijia.mq.service.IMqService;
import com.maijia.mq.service.MQConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * 几乎所有的操作都在channel中进行，channel是进行消息读写的通道。客户端可建立多个channel，每个channel代表一个会话任务。
 *
 * @author panjn
 * @date 2016/12/27
 */
public class Channel implements Serializable {

    private final transient Logger logger = Logger.getLogger(this.getClass());

    private String host = "localhost";
    private int port = -1;
    private String queueName;
    private String exchangeName = "default_exchange";
    private ExchangeType exchangeType = ExchangeType.DIRECT;
    private transient IMqService mqService;

    public Channel() {
    }

    public Channel(String host, int port) {
        this.host = host;
        this.port = port;
    }

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

    public String getQueueName() {
        return queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public IMqService getMqService() {
        return mqService;
    }

    public void setMqService(IMqService mqService) {
        this.mqService = mqService;
    }

    /**
     * Publish a message
     *
     * @param rawMsg
     * @throws IOException
     * @throws InterruptedException
     */
    public void basicPublish(Object rawMsg) throws IOException, InterruptedException {
        if (rawMsg == null) {
            throw new NullPointerException("message is NULL");
        }
        if (mqService == null) {
            throw new IllegalArgumentException("please set mqService first");
        }

        if (ExchangeType.FANOUT.equals(exchangeType)) {
            mqService.produce(this, rawMsg);
            return;
        }

        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("please declare target queue's name by method queueDeclare(String queueName) first");
        }

        if (ExchangeType.DIRECT.equals(exchangeType)) {
            //register exchange
            mqService.registerExchange(exchangeName, queueName);
        }

        mqService.produce(this, rawMsg);
    }

    /**
     * 消费消息，采用ACK确认机制
     *
     * @param consumer
     */
    public void basicConsume(MQConsumer consumer) throws IOException {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("please declare target queue's name by method queueDeclare(String queueName) first");
        }
        if (mqService == null) {
            throw new IllegalArgumentException("please set mqService first");
        }

        //register exchange
        mqService.registerExchange(exchangeName, queueName);

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
                if (obj instanceof Exception) {
                    logger.error(((Exception) obj).getMessage(), (Exception) obj);
                    break;
                }
                //进行消费
                consumer.handleDelivery((Message) obj);

                //确认消费成功 （写）
                os.writeObject("success");
                System.out.println("确认消费成功");

//                Thread.sleep(3000);
            }
        } catch (ConnectException e) {
            throw e;

        } catch (SocketException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                os.close(); //关闭Socket输出流
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            try {
                is.close(); //关闭Socket输入流
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            try {
                socket.close(); //关闭Socket
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Declare target queue'name
     *
     * @param queueName
     */
    public void queueDeclare(String queueName) {
        if (StringUtils.isBlank(queueName)) {
            throw new IllegalArgumentException("queueName is blank");
        }
        this.queueName = queueName;
    }

    /**
     * Declare exchange whit default type
     *
     * @param exchangeName
     */
    public void exchangeDeclare(String exchangeName) {
        if (StringUtils.isNotBlank(exchangeName)) {
            this.exchangeName = exchangeName;
        }
    }

    /**
     * Declare exchange and it's type
     * <p/>
     * if exchangeName equals 'default_exchange',then exchangeType remain default type
     *
     * @param exchangeName
     * @param exchangeType
     */
    public void exchangeDeclare(String exchangeName, ExchangeType exchangeType) {
        if (StringUtils.isNotBlank(exchangeName)) {
            this.exchangeName = exchangeName;
        }
        if (exchangeName.equals("default_exchange")) {
            return;
        }
        if (exchangeType != null) {
            this.exchangeType = exchangeType;
        }
    }

    public void close() {
        //todo
    }
}
