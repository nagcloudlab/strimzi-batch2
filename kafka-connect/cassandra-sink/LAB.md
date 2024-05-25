

start cassandra cluster and create keyspace and table

```bash
CREATE KEYSPACE IF NOT EXISTS my_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
USE my_keyspace;
CREATE TABLE IF NOT EXISTS users (
  id INT PRIMARY KEY,
  name TEXT,
  email TEXT
);
```


deploy kafka-connect-cassandra-sink-connector

```bash
curl -X POST -H "Content-Type: application/json" --data @connect-cassandra-sink.json http://localhost:8083/connectors
```


send messages to kafka topic

```bash
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic my_topic <<EOF
{"id": "1", "name": "Indu", "email": "indu@example.com"}
EOF
```
