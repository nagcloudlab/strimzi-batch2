package com.example;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerClient {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerClient.class);

    public static void main(String[] args) throws InterruptedException {
        KafkaProducer<String, String> producer = getKafkaProducer();
        List<String> languages = List.of("en", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh", "ar", "hi", "bn",
                "pa", "te", "mr", "ta", "ur", "gu", "kn", "ml", "sd", "ne", "si", "ps", "my", "am", "ceb", "jv", "ha",
                "yo", "uz", "mg", "so", "sn", "rw", "ku", "km", "lo", "bg", "uk", "pl", "ro", "nl", "el", "hu", "sv",
                "da", "fi", "sk", "cs", "et", "lt", "lv", "sl", "hr", "sr", "sq", "mk", "bs", "mt", "is", "ga", "cy",
                "eu", "gl", "ast", "ca", "ht", "tl", "xh", "zu", "ny", "st", "tn", "ss", "ve", "af", "nr", "xh", "zu",
                "ny", "st", "tn", "ss", "ve", "af", "nr", "sw", "rw", "lg", "mg", "sn", "so");
        int partition = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            String key = languages.get(i % languages.size());
            String value = "Hey Kafka!".repeat(100); // 1kb sized message

            ProducerRecord<String, String> record = new ProducerRecord<>("topic1", partition, key, value);
            partition += 3;
            if(partition==9){
                partition=0;
            }
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
            TimeUnit.MILLISECONDS.sleep(1);
        }
        producer.close();

    }

    private static KafkaProducer<String, String> getKafkaProducer() {
        Properties props = new Properties();
        // Client ID
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-client-1");

        // List of Kafka brokers to get metadata
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.49.2:30094");

//         props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
//         "strimzi.nagcloudlab.com:443");
//
//         props.put("security.protocol", "SSL");
//         props.setProperty("ssl.protocol", "TLSv1.2");
//         props.put("ssl.truststore.location",
//         "/home/nag/strimzi-batch2/security-1/kafka.client.truststore.jks");
//         props.put("ssl.truststore.password", "changeit");
////
//         props.put("ssl.keystore.location",
//         "/home/nag/strimzi-batch2/security-1/kafka.client.keystore.jks");
//         props.put("ssl.keystore.password", "changeit");

//
//        props.put("security.protocol", "SASL_PLAINTEXT");
//        props.put("sasl.mechanism","PLAIN");
//        props.put("sasl.jaas.config","org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");



        // Serializer class for key
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Serializer class for value
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Acknowledgments for message durability
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // Enable idempotence to avoid message duplication
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        // Retry settings
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
        // Maximum number of in-flight requests per connection
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        // Batch size and buffer memory
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // How long to wait before sending a batch in milliseconds
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // Compression type
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        // Buffer memory
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        // Max request size
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760);
        // Request timeout settings
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        // Delivery timeout settings
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        // Partitioner class
        // props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,
        // CustomPartitioner.class.getName());
        // Interceptor classes
        // props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
        // ProducerInterceptor.class.getName());
        // Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        return producer;
    }
}
