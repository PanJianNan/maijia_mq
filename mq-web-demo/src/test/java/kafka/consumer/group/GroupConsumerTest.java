//package kafka.consumer.group;
//
//import kafka.consumer.ConsumerConfig;
//import kafka.consumer.KafkaStream;
//import kafka.javaapi.consumer.ConsumerConnector;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.common.serialization.Serdes;
//import org.apache.kafka.streams.KafkaStreams;
//import org.apache.kafka.streams.StreamsBuilder;
//import org.apache.kafka.streams.StreamsConfig;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//public class GroupConsumerTest extends Thread {
//	private final KafkaConsumer<String, String> consumer;
//    private final String topic;
//    private  ExecutorService executor;
//
//	public GroupConsumerTest(String a_zookeeper, String a_groupId, String a_topic){
//        consumer = new KafkaConsumer(createConsumerConfig(a_zookeeper, a_groupId));
//        this.topic = a_topic;
//	}
//
//	public void shutdown() {
//        if (consumer != null) consumer.close();
//        if (executor != null) executor.shutdown();
//        try {
//            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
//                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
//            }
//        } catch (InterruptedException e) {
//            System.out.println("Interrupted during shutdown, exiting uncleanly");
//        }
//   }
//
//    public void run(int a_numThreads) {
//        Properties props = new Properties();
//        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application");
//        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.211.55.4:19092");
//        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//
//        StreamsBuilder builder = new StreamsBuilder();
//        builder.<String, String>stream("my-input-topic").mapValues(value -> value.toString()).to("my-output-topic");
//
//        ConsumerConnector
//        KafkaStreams streams = new KafkaStreams(builder.build(), props);
//        streams.start();
//
////        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
////        topicCountMap.put(topic, new Integer(a_numThreads));
////        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
////        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
////
////        // now launch all the threads
////        //
////        executor = Executors.newFixedThreadPool(a_numThreads);
////
////        // now create an object to consume the messages
////        //
////        int threadNumber = 0;
////        for (final KafkaStream stream : streams) {
////            executor.submit(new ConsumerTest(stream, threadNumber));
////            threadNumber++;
////        }
//    }
//	private static Properties createConsumerConfig(String a_zookeeper, String a_groupId) {
//        Properties props = new Properties();
//        props.put("zookeeper.connect", a_zookeeper);
//        props.put("group.id", a_groupId);
//        props.put("zookeeper.session.timeout.ms", "40000");
//        props.put("zookeeper.sync.time.ms", "2000");
//        props.put("auto.commit.interval.ms", "1000");
//
//        return props;
//    }
//
//	public static void main(String[] args) {
//		if(args.length < 1){
//			System.out.println("Please assign partition number.");
//		}
//
//        String zooKeeper = "10.211.50.4:12181,0.211.50.9:12181,0.211.50.10:12181";
//        String groupId = "grouptest";
//        String topic = "test";
//        int threads = Integer.parseInt(args[0]);
//
//		GroupConsumerTest example = new GroupConsumerTest(zooKeeper, groupId, topic);
//        example.run(threads);
//
//        try {
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException ie) {
//
//        }
//        example.shutdown();
//    }
//}
