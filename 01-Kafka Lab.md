
-------------------------------------
kafka cluster setup
-------------------------------------

1. verify java installation 
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
java -Dspring.config.additional-location=application.yml --add-opens java.rmi/javax.rmi.ssl=ALL-UNNAMED -jar lib/kafka-ui-api-v0.7.2.jar
```


-------------------------------------
Topic management 
-------------------------------------

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


ceate tppic with 3 partitions & 3 replicas

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic topic1 --partitions 3 --replication-factor 3
```

-------------------------------------
ConsumerGroup Management
-------------------------------------

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group group1
```

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consumer-group-3 --reset-offsets --to-earliest --execute --topic topic1
```

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consumer-group-3 --reset-offsets --to-latest --execute --topic topic1
```

```bash
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consumer-group-3 --reset-offsets --shift-by -100 --execute --topic topic1
```

---

-----------------------------------------------------
Security : encryption
-----------------------------------------------------


Steps:

1. Generate CA
2. Create Truststore
3. Create Keystore
4. Create certificate signing request (CSR)
5. Sign the CSR
6. Import the CA into Keystore
7. Import the signed certificate from step 5 into Keystore

-----------------------------------------------------

1. Generate CA
openssl req -new -x509 -keyout ca-key -out ca-cert -days 3650

-----------------------------------------------------
generate keystore and truststore for kafka broker-1
-----------------------------------------------------

2. Create Truststore
keytool -keystore kafka.broker-1.truststore.jks -alias ca-cert -import -file ca-cert

3. Create Keystore
keytool -keystore kafka.broker-1.keystore.jks -alias broker-1 -validity 3650 -genkey -keyalg RSA -ext SAN=dns:localhost

4. Create certificate signing request (CSR)
keytool -keystore kafka.broker-1.keystore.jks -alias broker-1 -certreq -file ca-request-broker-1

5. Sign the CSR
openssl x509 -req -CA ca-cert -CAkey ca-key -in ca-request-broker-1 -out ca-signed-broker-1 -days 3650 -CAcreateserial

6. Import the CA into Keystore
keytool -keystore kafka.broker-1.keystore.jks -alias ca-cert -import -file ca-cert

7. Import the signed certificate from step 5 into Keystore
keytool -keystore kafka.broker-1.keystore.jks -alias broker-1 -import -file ca-signed-broker-1

----------------------------------------------------------
generate keystore and truststore for kafka client
----------------------------------------------------------

2. Create Truststore
keytool -keystore kafka.client.truststore.jks -alias ca-cert -import -file ca-cert

3. Create Keystore
keytool -keystore kafka.client.keystore.jks -alias client -validity 3650 -genkey -keyalg RSA -ext SAN=dns:localhost

4. Create certificate signing request (CSR)
keytool -keystore kafka.client.keystore.jks -alias client -certreq -file ca-request-client

5. Sign the CSR
openssl x509 -req -CA ca-cert -CAkey ca-key -in ca-request-client -out ca-signed-client -days 3650 -CAcreateserial

6. Import the CA into Keystore
keytool -keystore kafka.client.keystore.jks -alias ca-cert -import -file ca-cert

7. Import the signed certificate from step 5 into Keystore
keytool -keystore kafka.client.keystore.jks -alias client -import -file ca-signed-client

----------------------------------------------------------


broker-1 server.properties

inter broker communication over plaintext & client communication over ssl
with listener.security.protocol.map

```properties
listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
advertised.listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL
ssl.keystore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.keystore.jks
ssl.keystore.password=broker-1
ssl.key.password=broker-1
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.truststore.jks
ssl.truststore.password=broker-1
```


kafka-client properties

```properties
security.protocol=SSL
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.client.truststore.jks
ssl.truststore.password=broker-1
```


-----------------------------------------------------
Security : mutual authentication (mTLS) 
-----------------------------------------------------


broker-1 server.properties

inter broker communication over plaintext & client communication over ssl
with listener.security.protocol.map

```properties
listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
advertised.listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL
ssl.keystore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.keystore.jks
ssl.keystore.password=broker-1
ssl.key.password=broker-1

ssl.client.auth=required
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.truststore.jks
ssl.truststore.password=broker-1

```

kafka-client properties

```properties
security.protocol=SSL
ssl.keystore.location=/home/centos/kafka_2.13-3.7.0/kafka.client.keystore.jks
ssl.keystore.password=broker-1
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.client.truststore.jks
ssl.truststore.password=broker-1
```



-----------------------------------------------------
Security : ACL
-----------------------------------------------------

broker-1 server.properties

```properties
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
super.users=User:ANONYMOUS
```



```bash
keytool -list -v -keystore kafka.client.keystore.jks | grep "Owner"
```
Owner: CN=localhost, OU=tng, O=cloudlab, L=chennai, ST=TN, C=IN
then
principal name is User:CN=localhost, OU=tng, O=cloudlab, L=chennai, ST=TN, C=IN


create ACL for a topic topic1, allow principal to read & write & get metadata

```bash
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:CN=localhost,OU=tng,O=cloudlab,L=chennai,ST=TN,C=IN --operation Read --operation Write --operation Describe --topic topic1
```

list ACLs
```bash
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --list
```

delete ACL
```bash
bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 --remove --allow-principal User:CN=localhost,OU=tng,O=cloudlab,L=chennai,ST=TN,C=IN --operation Read --operation Write --operation Describe --topic topic1
```


-----------------------------------------------------
Security : SASL_PLAINTEXT
-----------------------------------------------------

broker-1 server.properties

```properties
listeners=SASL_PLAINTEXT://localhost:9092
advertised.listeners=SASL_PLAINTEXT://localhost:9092
sasl.enabled.mechanisms=PLAIN
sasl.mechanism.inter.broker.protocol=PLAIN
security.inter.broker.protocol=SASL_PLAINTEXT
```


kafka_server_jaas.conf

```conf
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_alice="alice-secret";
};
```

```bash
export KAFKA_OPTS="-Djava.security.auth.login.config=/home/nag/strimzi-batch2/kafka_2.13-3.7.0/config/kafka_server_jaas.conf"
```


client.properties

```properties

security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";
```

-----------------------------------------------------
Security : SASL_SSL ( recommended )
-----------------------------------------------------

broker-1 server.properties

```properties
listeners=SASL_SSL://localhost:9092
advertised.listeners=SASL_SSL://localhost:9092
sasl.enabled.mechanisms=PLAIN
sasl.mechanism.inter.broker.protocol=PLAIN
security.inter.broker.protocol=SASL_SSL
ssl.keystore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.keystore.jks
ssl.keystore.password=broker-1
ssl.key.password=broker-1
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.broker-1.truststore.jks
ssl.truststore.password=broker-1
```

kafka_server_jaas.conf

```conf
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_alice="alice-secret";
```

client.properties

```properties
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";
ssl.truststore.location=/home/centos/kafka_2.13-3.7.0/kafka.client.truststore.jks
ssl.truststore.password=broker-1
```

-----------------------------------------------------
schema registry
-----------------------------------------------------

```json
{
  "type": "record",
  "name": "Purchase",
  "namespace": "io.confluent.developer.avro",
  "fields": [
    {
      "name": "item",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    },
    {
      "name": "total_cost",
      "type": "double"
    },
    {
      "name": "customer_id",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    }
  ]
}
```

register above schema in schema registry by curl command

```bash
curl -X POST -H "Content-Type: application/json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"Purchase\",\"namespace\":\"io.confluent.developer.avro\",\"fields\":[{\"name\":\"item\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"total_cost\",\"type\":\"double\"},{\"name\":\"customer_id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}"}' \
  http://localhost:8081/subjects/purchases-value/versions
```

evolve the schema by adding a new field `purchase_date` of type `string` to the schema

```json
{
  "type": "record",
  "name": "Purchase",
  "namespace": "io.confluent.developer.avro",
  "fields": [
    {
      "name": "item",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    },
    {
      "name": "total_cost",
      "type": "double"
    },
    {
      "name": "customer_id",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    },
    {
      "name": "purchase_date",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    }
  ]
}
```

register the evolved schema in schema registry by curl command

```bash
curl -X POST -H "Content-Type: application/json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"Purchase\",\"namespace\":\"io.confluent.developer.avro\",\"fields\":[{\"name\":\"item\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"total_cost\",\"type\":\"double\"},{\"name\":\"customer_id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"purchase_date\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}"}' \
  http://localhost:8081/subjects/purchases-value/versions
```

evolve the schema by adding a new field `purchase_date` of type `string` to the schema and setting a default value for the field

```json
{
  "type": "record",
  "name": "Purchase",
  "namespace": "io.confluent.developer.avro",
  "fields": [
    {
      "name": "item",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    },
    {
      "name": "total_cost",
      "type": "double"
    },
    {
      "name": "customer_id",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      }
    },
    {
      "name": "purchase_date",
      "type": {
        "type": "string",
        "avro.java.string": "String"
      },
      "default": "2021-01-01"
    }
  ]
}
```

register the evolved schema in schema registry by curl command

```bash
curl -X POST -H "Content-Type: application/json" \
  --data '{"schema": "{\"type\":\"record\",\"name\":\"Purchase\",\"namespace\":\"io.confluent.developer.avro\",\"fields\":[{\"name\":\"item\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"total_cost\",\"type\":\"double\"},{\"name\":\"customer_id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"purchase_date\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"},\"default\":\"2021-01-01\"}]}"}' \
  http://localhost:8081/subjects/purchases-value/versions
```
