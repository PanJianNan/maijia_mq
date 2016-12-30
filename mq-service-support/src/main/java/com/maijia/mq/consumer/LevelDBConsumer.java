package com.maijia.mq.consumer;

import com.maijia.mq.domain.Message;
import com.maijia.mq.leveldb.LevelDBPersistenceAdapter;
import com.maijia.mq.leveldb.LevelDBQueue;
import com.maijia.mq.leveldb.QueueMiddleComponent;
import com.maijia.mq.leveldb.strategy.LimitReadHouseKeepingStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * LevelDBConsumer
 *
 * @author panjn
 * @date 2016/12/6
 */
@Service
public class LevelDBConsumer implements Consumer {

    @Resource
    LevelDBPersistenceAdapter adapter;

    @Override
    public Message take(String queueName) throws IOException, InterruptedException {
        LevelDBQueue levelDBQueue = this.getLevelDBQueue(queueName);
        return levelDBQueue.take();
    }

    @Override
    public Message poll(String queueName) throws IOException, InterruptedException {
        LevelDBQueue levelDBQueue = this.getLevelDBQueue(queueName);
        return levelDBQueue.poll();
    }

    /**
     * get leveldb queue
     *
     * @param queueName
     * @return
     * @throws IOException
     */
    private LevelDBQueue getLevelDBQueue(String queueName) throws IOException {
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
        return levelDBQueue;
    }
}
