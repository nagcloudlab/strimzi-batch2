


1 zookeeper & 3 broker cluster setup
-------------------------------------

1. verify java version
```bash
java -version
```

2. download kafka
```bash
wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz
tar -xvzf kafka_2.13-3.7.0.tgz
```

3. start zookeeper
```bash
cd kafka_2.13-3.7.0
bin/zookeeper-server-start.sh config/zookeeper.properties
```

4. start zookeeper-shell
```bash
bin/zookeeper-shell.sh localhost:2181
```

5. start kafka brokers
```bash
bin/kafka-server-start.sh config/server-101.properties
bin/kafka-server-start.sh config/server-102.properties
bin/kafka-server-start.sh config/server-103.properties
```

6. kafka-ui
```bash
cd kafka-ui
wget https://github.com/provectus/kafka-ui/releases/download/v0.7.2/kafka-ui-api-v0.7.2.jar
touch application.yml
```


```yaml
kafka:
  clusters:
    - name: local
      bootstrapServers: localhost:9092
```

```bash
java -Dspring.config.additional-location=application.yml --add-opens java.rmi/javax.rmi.ssl=ALL-UNNAMED -jar kafka-ui-api-v0.7.2.jar
```



topic management with cluster
-----------------------------

option-1: admin-scripts

list topics
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 
```

create topic
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic1
```

describe topic
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic topic1
```

create topic with partition
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic2 --partitions 2
bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic topic3 --partitions 3
```

delete topic
```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic topic1
```

option-2: kafka-ui

---

ceate tppic with 3 partitions & 3 replicas

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic1 --partitions 3 --replication-factor 3
```