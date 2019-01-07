package kafka.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.time.Duration;
import java.util.*;

/**
 * Created by panjiannan on 2018/8/14.
 */
public class SimpleConsumerTest {

    //自动提交offset
    @Test
    public void autoGroupConsume() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092,10.211.55.9:19092,10.211.55.10:19092");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "c3");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "g1");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        Consumer<String, String> consumer = new KafkaConsumer<>(props);
//        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());//如果props中没指定，则需要在构造函数中传入
        consumer.subscribe(Arrays.asList("part2"));
        while (true) {
//            ConsumerRecords<String, String> records = consumer.poll(100);//过时的
            Duration duration = Duration.ofMillis(100L);
            ConsumerRecords<String, String> records = consumer.poll(duration);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }

    //手动提交offset
    //可以确保消息至少被消费一次，因为如果commitSync方法提交失败了，那么将导致这部分消息被重复的消费。
    //在这个例子中，我们将消费一批消息并将它们存储在内存中。当我们积累足够多的消息后，我们再将它们批量插入到数据库中。如果我们设置offset自动提交（之前说的例子），消费将被认为是已消费的。这样会出现问题，我们的进程可能在批处理记录之后，但在它们被插入到数据库之前失败了。
    @Test
    public void manualGroupConsume() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");手动提交时不需要
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("foo", "bar"));

        final int minBatchSize = 10;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            Duration duration = Duration.ofMillis(100L);
            ConsumerRecords<String, String> records = consumer.poll(duration);

            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }

            if (buffer.size() >= minBatchSize) {
                //insertIntoDb(buffer);//
                consumer.commitSync();
                buffer.clear();
            }
        }

        //精细到分区的消费控制
        /*while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));

            for (TopicPartition partition : records.partitions()) {
                List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                for (ConsumerRecord<String, String> record : partitionRecords) {
                    System.out.println(record.offset() + ": " + record.value());
                }
                long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                //注意：已提交的offset应始终是你的程序将读取的下一条消息的offset。因此，调用commitSync（offsets）时，你应该加1个到最后处理的消息的offset。
                consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
            }
        }*/
    }


    //手动选择分区消费
    //但是此时如果消费者失败了，kafka不会将你之前消费的partition重新分配给其他消费者，因此不会出现rebalance。
    //同时用户也应该确保每个消费者消费的partition是不重合的。
    @Test
    public void manualPartitionConsume() {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "c2");
        props.put("bootstrap.servers", "10.211.55.4:19092");
        props.put("group.id", "g1");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); //关闭自动提交
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        String topic = "streams-wordcount-output";
        TopicPartition partition0 = new TopicPartition(topic, 0);
        TopicPartition partition1 = new TopicPartition(topic, 1);
        consumer.assign(Arrays.asList(partition0, partition1));
        consumer.seekToBeginning(Arrays.asList(partition0, partition1));//从0开始消费
//        consumer.seek(partition0, 0);//指定分区偏移量

        final int minBatchSize = 200;
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100L));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
                    System.out.println(new Date());
                }
                for (ConsumerRecord<String, String> record : records) {
                    buffer.add(record);
                }
                if (buffer.size() >= minBatchSize) {
//                insertIntoDb(buffer);
                    consumer.commitSync();
                    buffer.clear();
                }
            }
        } catch (Exception e) {
            consumer.close();
        }

    }
}
