package com.example;

import java.util.Map;

import org.apache.kafka.common.Cluster;

public class CustomPartitioner implements org.apache.kafka.clients.producer.Partitioner {

    @Override
    public void configure(Map<String, ?> configs) {
    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // partition logic
        return 0;
    }

    @Override
    public void close() {
    }

}
