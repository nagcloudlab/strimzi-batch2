package com.example;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerClient {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerClient.class);

    public static void main(String[] args) throws InterruptedException {
        // Metadadata Request
        Properties props = new Properties();
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaProducerClient");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // broker-101
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "com.example.CustomPartitioner"); // Custom Partitioner

        // Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props); // Metadata Request
        List<String> languages=List.of("en","es","fr","de","it","pt","ru","ja","ko","zh","ar","hi","bn","pa","te","mr","ta","ur","gu","kn","ml","sd","ne","si","ps","my","am","ceb","jv","ha","yo","uz","mg","so","sn","rw","ku","km","lo","bg","uk","pl","ro","nl","el","hu","sv","da","fi","sk","cs","et","lt","lv","sl","hr","sr","sq","mk","bs","mt","is","ga","cy","eu","gl","ast","ca","ht","tl","xh","zu","ny","st","tn","ss","ve","af","nr","xh","zu","ny","st","tn","ss","ve","af","nr","sw","rw","lg","mg","sn","so");
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String key = languages.get(i % languages.size());
            String value = "Hey Kafka!".repeat(100); // 1kb message
            ProducerRecord<String, String> record = new ProducerRecord<>("topic1", 1, key, value);
            producer.send(record, (recordMetadata, exception) -> {
                if (exception == null) {
                    logger.info("Received new metadata \nTopic: {}\nKey: {}\nPartition: {}\nOffset: {}\nTimestamp: {}",
                            recordMetadata.topic(),
                            key,
                            recordMetadata.partition(),
                            recordMetadata.offset(),
                            recordMetadata.timestamp());
                } else {
                    logger.error("Error while producing: {}", exception.getMessage());
                }
            });
            TimeUnit.SECONDS.sleep(1);
        }
        producer.close();

    }
}
