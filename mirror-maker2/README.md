# Kafka MirrorMaker 2 Demo

[Kafka v2.4.0][1] introduced MirrorMaker 2.0 (MM2); a new multi-cluster, cross-datacenter replication engine. MirrorMaker 2.0 is well documented in the [KIP-382][2] proposal and the [MirrorMaker 2.0 `README.md`][3], but I learn-by-doing.

This demo sets up 2 local, single-broker Kafka clusters and one MirrorMaker 2.0 instance to replicate topics from cluster A to cluster B. Once you have this environment set up, it's quick and easy to play with different configuration options.

| Clusters | Topic | Kafka Port | ZooKeeper Port |
| -------- | ----- | ---------- | -------------- |
| A        | t1    | 9091       | 2181           |
| B        | t2    | 9092       | 2182           |

![Diagram]

## Set up

1. Clone this repository and `cd` into the directory.

1. Download a [Kafka release](https://kafka.apache.org/downloads) and extract it to a `./kafka/`.

1. Clean up, if you've run this before.

   ```sh
   rm -rf /tmp/zk1
   rm -rf /tmp/zk2
   rm -rf /tmp/kafka-logs-1
   rm -rf /tmp/kafka-logs-2
   ```

1. Run each of the following commands in a separate shell to start your two single-node Kafka clusters (each with their own single-node ZooKeeper instance).

   ```sh
   ./bin/zookeeper-server-start.sh config/zk1.properties
   ./bin/kafka-server-start.sh config/server1.properties
   ./bin/zookeeper-server-start.sh config/zk2.properties
   ./bin/kafka-server-start.sh config/server2.properties
   ```

1. Create topics.

   ```sh
   ./bin/kafka-topics.sh --create --topic t1 --bootstrap-server localhost:9091 --partitions 1 --replication-factor 1
   ./bin/kafka-topics.sh --create --topic t2 --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
   ```

1. Start MirrorMaker.

   ```sh
   ./bin/connect-mirror-maker.sh config/mm2.properties
   ```

## Common tasks

```sh
# List topics
./bin/kafka-topics.sh --list --bootstrap-server localhost:9091
./bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Produce
./bin/kafka-console-producer.sh --topic t1 --bootstrap-server localhost:9091
./bin/kafka-console-producer.sh --topic t2 --bootstrap-server localhost:9092

# Consume
./bin/kafka-console-consumer.sh --topic t1 --bootstrap-server localhost:9091
./bin/kafka-console-consumer.sh --topic t2 --bootstrap-server localhost:9092

# Consumer replicated topic
./bin/kafka-console-consumer.sh --topic A.t1 --bootstrap-server localhost:9092
```
