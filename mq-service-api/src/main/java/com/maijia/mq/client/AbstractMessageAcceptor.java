package com.maijia.mq.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/**
 * AbstractMessageAcceptor
 *
 * @author panjn
 * @date 2016/12/29
 */
public abstract class AbstractMessageAcceptor {

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected void fire() {
        //创建负责消费的线程
        Thread thread = new Thread(() -> {
            try {
                logger.info("init message acceptor");
                link();
            } catch (IOException e) {
                logger.info("消费异常，断开连接");
                logger.error(e.getMessage(), e);
                try {
                    retryLink();
                } catch (Exception e1) {
                    logger.info("消费由于异常而失败");
                    logger.error(e1.getMessage(), e1);
                }
            }
        }, this.getClass().getName() + "-message-acceptor-thread");
        thread.start();
    }

    protected abstract void link() throws IOException;

    protected void retryLink() throws IOException {
        try {
            logger.info("===========尝试重连MJMQ==========");
            link();//todo 之前是通过捕获ConnectionException才尝试重连的,但是SocketException和EOFException也可能需要重连，不过现在可能引起嵌套深度过深导致栈溢出，可以尝试设置一个重试上限次数来解决
        } catch (IOException e) {
            logger.info("===========重连MJMQ失败，1分钟后重试！==========");
            try {
                TimeUnit.MINUTES.sleep(1L);
                retryLink();
            } catch (InterruptedException e1) {
                logger.error(e1);
            }
        }
    }
}
