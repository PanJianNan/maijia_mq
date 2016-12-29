package com.maijia.mq.client;

import com.alibaba.dubbo.rpc.RpcException;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * AbstractMessageAcceptor
 *
 * @author panjn
 * @date 2016/12/29
 */
public abstract class AbstractMessageAcceptor {

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected void fire() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("init message acceptor");
                    link();
                    retryLink();
                    logger.info("消费异常，断开连接");
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    protected abstract void link() throws IOException;

    protected void retryLink() throws IOException {
        try  {
            logger.info("===========尝试重连MJMQ==========");
            link();
        } catch (RpcException e) {
            logger.info("===========重连MJMQ失败，1分后重试！==========");
            try {
                Thread.sleep(60 * 1000);
                retryLink();
            } catch (InterruptedException e1) {
                logger.error(e1);
            }
        }
    }
}
