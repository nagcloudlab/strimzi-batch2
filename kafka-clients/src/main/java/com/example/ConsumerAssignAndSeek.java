package com.example.basics;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerAssignAndSeek {
    static Logger log = LoggerFactory.getLogger("kp");

    public static void main(String[] args) {

        String bootstrapServers = "127.0.0.1:9092";
        String topic = "topic1";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // assign
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        consumer.assign(Arrays.asList(topicPartition));

        long offsetToReadFrom = 1L;

        // seek
        consumer.seek(topicPartition, offsetToReadFrom);

        int numberOfMessagesToRead = 3;
        boolean keepOnReading = true;
        int numberOfMessagesReadSoFar = 0;


        while (keepOnReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                numberOfMessagesReadSoFar += 1;
                log.info("Key: " + record.key() + ", Value: " + record.value());
                log.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                if (numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
                    keepOnReading = false; // to exit the while loop
                    break; // to exit the for loop
                }
            }
        }
        consumer.close();
    }

}