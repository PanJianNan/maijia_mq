package com.maijia.mq.producer;


import com.maijia.mq.domain.Message;
import com.maijia.mq.leveldb.LevelDBPersistenceAdapter;
import com.maijia.mq.leveldb.LevelDBQueue;
import com.maijia.mq.leveldb.QueueMiddleComponent;
import com.maijia.mq.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

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

    public boolean produce(String queueName, Object rawMsg) throws IOException, InterruptedException {
        LevelDBQueue levelDBQueue = QueueMiddleComponent.QUEUE_MAP.get(queueName);

        if (levelDBQueue == null) {
            synchronized (this) {
                levelDBQueue = QueueMiddleComponent.QUEUE_MAP.get(queueName);
                //如果levelDBQueue是null，则创建
                if (levelDBQueue == null) {
                    //初始化
                    QueueMiddleComponent queueMiddleComponent = new QueueMiddleComponent(adapter, queueName);
                    levelDBQueue = new LevelDBQueue(queueMiddleComponent);
                    LimitReadHouseKeepingStrategy strategy = new LimitReadHouseKeepingStrategy(queueMiddleComponent);
//                    strategy.setCheckInterval(60 * 1000L);
                    levelDBQueue.setHouseKeepingStrategy(strategy);
                    levelDBQueue.connectLevelDB();
                    QueueMiddleComponent.QUEUE_MAP.put(queueName, levelDBQueue);
                }
            }
        }

        Message message = new Message(rawMsg);
        levelDBQueue.put(message);//消息落地

        return true;
    }

}
