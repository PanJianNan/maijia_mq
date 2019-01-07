package kafka.producer;

import org.apache.commons.lang3.ThreadUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.junit.Test;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by panjiannan on 2018/8/14.
 */
public class SimpleProducerTest {

    //异步发送消息
    //当前例子，在发送消息还没达到batch.size的大小时，等待1秒，将聚合好后的消息发送（当前效果是1秒后10条消息都发送出去）
    @Test
    public void asynProduce() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092,10.211.55.9:19092,10.211.55.10:19092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "p1");
        //acks 0:不等确认，1：leader写入消息到本地日志就立即响应，而不等待所有follower应答。（leader的分区收到消息并确认后，还没来得及复制到其它follower上，消息就会丢失），all(或者-1)：leader将等待所有副本同步后应答消息
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);//发送延迟毫秒数，默认是0即不延迟，当然如果消息达到batch.size的大小就不需要继续等待延迟而是直接发送了
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            //异步发送
            producer.send(new ProducerRecord<>("part3", Integer.toString(i), Integer.toString(i)));
        }

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        producer.close();

    }

    //同步发送消息
    //如果使用同步发送，每条消息发送间隔linger.ms，当前例子每隔1秒同步发送一条消息（当前效果是发送10条消息耗时10秒）吞吐量是很低的，所有要谨慎
    @Test
    public void synProduce() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092,10.211.55.9:19092,10.211.55.10:19092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "p1");
        //acks 0:不等确认，1：leader写入消息到本地日志就立即响应，而不等待所有follower应答。（leader的分区收到消息并确认后，还没来得及复制到其它follower上，消息就会丢失），all(或者-1)：leader将等待所有副本同步后应答消息
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);//发送延迟毫秒数，默认是0即不延迟，当然如果消息达到batch.size的大小就不需要继续等待延迟而是直接发送了
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            Future future = producer.send(new ProducerRecord<>("part2", Integer.toString(i), Integer.toString(i)), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println(metadata);
                }
            });
            try {
                future.get();//异步转同步，即需要等待异步执行结果回调才继续往下执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println("send success: "+ new Date());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        producer.close();

    }

    //事务提交消息
    //提交10条消息，要么全部提交成功，要么全部失败
    @Test
    public void transactionProduce() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092,10.211.55.9:19092,10.211.55.10:19092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "p1");
//        props.put(ProducerConfig.ACKS_CONFIG, "all");
//        props.put(ProducerConfig.RETRIES_CONFIG, 0);
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
//        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
//        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "panjn-transactional-id");

        Producer<String, String> producer = new KafkaProducer<>(props);
        try {
            producer.initTransactions();//mmp的，kafka官方javadoc实例在beginTransaction前居然漏了这一句，直接报事务未初始化的错
            producer.beginTransaction();
            for (int i = 0; i < 10; i++) {
//                producer.send(new ProducerRecord<>("part2", Integer.toString(i), Integer.toString(i)));
                //异步发送模式
                producer.send(new ProducerRecord<>("part2", Integer.toString(i), Integer.toString(i)),
                        new Callback() {
                            public void onCompletion(RecordMetadata metadata, Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                } else {
                                    System.out.println("partition:" + metadata.partition() + "  The offset of the record we just sent is: " + metadata.offset());
                                    System.out.println(metadata);
                                }
                            }
                        });
            }
            producer.commitTransaction();
        } catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            // We can't recover from these exceptions, so our only option is to close the producer and exit.
            producer.close();
        } catch (KafkaException e) {
            // For all other exceptions, just abort the transaction and try again.
            producer.abortTransaction();
        }
        producer.close();
    }


    //手动选择分区发送消息
    // 指定的分区如果不在topic拥有的分区集合里会报错
    // 如果再加上同步发送消息，就能确保分区内消息的顺序性，这也是kafka消息顺序性的实现，所谓的顺序性只是对于一个分区来说的
    @Test
    public void manualPartitionProduce() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092,10.211.55.9:19092,10.211.55.10:19092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord<>("part2", 3, Integer.toString(i), Integer.toString(i)));
            //feature.get()//如果同步发送消息，那么part2的序号为3的分区里的消息就可以确保顺序性了
        }

        producer.close();
    }

}
