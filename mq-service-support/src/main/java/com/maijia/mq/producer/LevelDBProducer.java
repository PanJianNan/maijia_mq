package com.maijia.mq.producer;


import com.maijia.mq.leveldb.LevelDBPersistenceAdapter;
import com.maijia.mq.leveldb.LevelDBQueue;
import com.maijia.mq.leveldb.QueueMiddleComponent;
import com.maijia.mq.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * LevelDBProducer
 *
 * @author panjn
 * @date 2016/10/20
 */
@Service
public class LevelDBProducer implements Producer {

    @Resource
    LevelDBPersistenceAdapter adapter;

    public boolean produce(String queueName, Object msg) throws IOException, InterruptedException {
        LevelDBQueue levelDBQueue = QueueMiddleComponent.QUEUE_MAP.get(queueName);
        if (levelDBQueue == null) {
            //初始化
            QueueMiddleComponent queueMiddleComponent = new QueueMiddleComponent(adapter, queueName);
            levelDBQueue = new LevelDBQueue(queueMiddleComponent);
            LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queueMiddleComponent);
            strategy.setCheckInterval(1000);
            levelDBQueue.setHouseKeepingStrategy(strategy);
            levelDBQueue.open();
            QueueMiddleComponent.QUEUE_MAP.put(queueName, levelDBQueue);
        }

        String eventId = UUID.randomUUID().toString();
//        levelDBQueue.put(new CommonEventSource("Mars", eventId, "msg:" + msg, null, null, null));//消息落地
        levelDBQueue.put(msg);//消息落地

        return true;
    }

  /*  public EventSourceBase take(String queueName) throws Exception {
        LevelDBQueue levelDBQueue = QueueMiddleComponent.QUEUE_MAP.get(queueName);
        if (levelDBQueue == null) {
            //初始化
            QueueMiddleComponent queueMiddleComponent = new QueueMiddleComponent(adapter, queueName);
            levelDBQueue = new LevelDBQueue(queueMiddleComponent);
            LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queueMiddleComponent);
            strategy.setCheckInterval(1000);
            levelDBQueue.setHouseKeepingStrategy(strategy);
            levelDBQueue.open();
            QueueMiddleComponent.QUEUE_MAP.put(queueName, levelDBQueue);
        }
        return levelDBQueue.take();
    }

    public static void main(String[] args) throws Exception {
        final LevelDBProducer producer = new LevelDBProducer();
        final Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(3000);
                        Random random = new Random();
                        System.out.println("=开生1");
                        producer.produce("pjn", random.nextInt(100));
                        System.out.println("生完1=");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        writeThread.start();

        final Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("开始消费1");
                        EventSourceBase evt  = producer.take("test");
                        System.out.println(evt.getEventName());
                        System.out.println("结束消费1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        readThread.start();

    }*/
}
