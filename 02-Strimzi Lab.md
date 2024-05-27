

------------------------------------------------------------
#1 install strimzi operator
------------------------------------------------------------

```bash
kubectl create namespace kafka
kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka
```

or

```bash
wget https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.41.0/strimzi-0.41.0.tar.gz
tar -xvzf strimzi-0.41.0.tar.gz
cd strimzi-0.41.0
kubectl create namespace kafka
sed -i 's/namespace: .*/namespace: kafka/' install/cluster-operator/*RoleBinding*.yaml
kubectl apply -f install/cluster-operator -n kafka
kubectl get deployment -n kafka -w
kubectl logs deployment/strimzi-cluster-operator -n kafka -f
```

operator replicas & leader election

```bash
kubectl edit deployment strimzi-cluster-operator -n kafka
kubectl scale deployment strimzi-cluster-operator --replicas=2 -n kafka
kubectl get pods -n kafka
kubectl logs <pod> -n kafka -f
kuebctl delete pod <pod> -n kafka
```


------------------------------------------------------------
Affinity & Anti-Affinity
------------------------------------------------------------

```bash
kubectl get nodes -o wide
kubectl taint nodes aks-nodepool1-17982226-vmss000000 dedicated=Kafka:NoSchedule
kubectl taint nodes aks-nodepool1-17982226-vmss000001 dedicated=Kafka:NoSchedule
kubectl taint nodes aks-nodepool1-17982226-vmss000002 dedicated=Kafka:NoSchedule

kubectl label nodes aks-nodepool1-17982226-vmss000000 dedicated=Kafka
kubectl label nodes aks-nodepool1-17982226-vmss000001 dedicated=Kafka
kubectl label nodes aks-nodepool1-17982226-vmss000002 dedicated=Kafka

kubectl get nodes --show-labels
```

------------------------------------------------------------
Deploy kafka cluster
------------------------------------------------------------

```bash

kubectl apply -f ./kafka.yaml -n kafka
kubectl get svc -n kafka
kubectl get ingress -n kafka
kubectl describe ingress my-cluster-kafka-tlsingress-bootstrap -n kafka

```


------------------------------------------------------------
SSL/TLS Configuration
------------------------------------------------------------

```bash
export CLUSTER_NAME=my-cluster
kubectl get secret $CLUSTER_NAME-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.crt}' | base64 -d > ca.crt
kubectl get secret $CLUSTER_NAME-cluster-ca-cert -n kafka -o jsonpath='{.data.ca\.password}' | base64 --decode > ca.password

export CERT_FILE_PATH=ca.crt
export CERT_PASSWORD_FILE_PATH=ca.password
export PASSWORD=`cat $CERT_PASSWORD_FILE_PATH`
export CA_CERT_ALIAS=strimzi-kafka-cert
sudo keytool -importcert -alias $CA_CERT_ALIAS -file $CERT_FILE_PATH -keystore kafka-truststore.jks -keypass $PASSWORD
sudo keytool -list -alias $CA_CERT_ALIAS -keystore kafka-truststore.jks
```

------------------------------------------------------------
mutual TLS authentication
------------------------------------------------------------

```bash
export CLUSTER_NAME=my-cluster
kubectl get configmap/${CLUSTER_NAME}-pool-a-0 -o yaml -n kafka
kubectl get deployment $CLUSTER_NAME-entity-operator -n kafka
kubectl get pod -l=app.kubernetes.io/name=entity-operator -n kafka


kubectl apply -n kafka -f ../kafka-user1.yaml 

kubectl get secret/user1 -o yaml -n kafka
export KAFKA_USER_NAME=user1
kubectl -n kafka get secret $KAFKA_USER_NAME -o jsonpath='{.data.user\.crt}' | base64 --decode > user.crt 
kubectl -n kafka get secret $KAFKA_USER_NAME -o jsonpath='{.data.user\.key}' | base64 --decode > user.key
kubectl -n kafka get secret $KAFKA_USER_NAME -o jsonpath='{.data.user\.p12}' | base64 --decode > user.p12
kubectl -n kafka get secret $KAFKA_USER_NAME -o jsonpath='{.data.user\.password}' | base64 --decode > user.password


# Import the entry in user.p12 into another keystore

export USER_P12_FILE_PATH=user.p12
export USER_KEY_PASSWORD_FILE_PATH=user.password
export KEYSTORE_NAME=kafka-keystore.jks
export KEYSTORE_PASSWORD=changeit
export PASSWORD=`cat $USER_KEY_PASSWORD_FILE_PATH`


sudo keytool -importkeystore -deststorepass $KEYSTORE_PASSWORD -destkeystore $KEYSTORE_NAME -srckeystore $USER_P12_FILE_PATH -srcstorepass $PASSWORD -srcstoretype PKCS12
sudo keytool -list -alias $KAFKA_USER_NAME -keystore $KEYSTORE_NAME

kubectl apply -f ../kafka-topic1.yaml -n kafka
kubectl apply -f ../kafka-topic2.yaml -n kafka


```


------------------------------------------------------------
Strimzi Kafka OAuth using Keycloak
------------------------------------------------------------

```bash
kubectl apply -f ./keycloak.yaml -n kafka
kubectl get svc -n kafka
```

```bash
kubectl apply -f ./kafka-oauth.yaml -n kafka
```

```bash

kubectl get pods -n kafka
kubectl exec -n kafka --stdin --tty my-cluster-kafka-0 -- /bin/bash

cat > ~/team-a-client.properties << EOF
security.protocol=SASL_PLAINTEXT
sasl.mechanism=OAUTHBEARER
sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required \
  oauth.client.id="team-a-client" \
  oauth.client.secret="team-a-client-secret" \
  oauth.token.endpoint.uri="http://keycloak:8080/auth/realms/kafka-authz/protocol/openid-connect/token" ;
sasl.login.callback.handler.class=io.strimzi.kafka.oauth.client.JaasClientOauthLoginCallbackHandler
EOF


cd /opt/kafka/bin
ls

./kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic my-topic --producer.config=/home/kafka/team-a-client.properties
./kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic a_messages --producer.config=/home/kafka/team-a-client.properties


./kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic a_messages --from-beginning --consumer.config=/home/kafka/team-a-client.properties
./kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic a_messages --from-beginning --consumer.config=/home/kafka/team-a-client.properties --group a_consumer_group_1

```


-----------------------------------------------------
Deploy kafka-Exporter, Prometheus, Grafana
-----------------------------------------------------

**Updating kafka Custom Resource for Kafka Exporter & Prometheus Monitoring**

**Deploying the Prometheus Operator**

```bash
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/bundle.yaml > ./strimzi-0.41.0/examples/metrics/prometheus-operator-deployment.yaml
sed -E -i '/[[:space:]]*namespace: [a-zA-Z0-9-]*$/s/namespace:[[:space:]]*[a-zA-Z0-9-]*$/namespace: kafka/' ./strimzi-0.41.0/examples/metrics/prometheus-operator-deployment.yaml
kubectl create -f ./strimzi-0.41.0/examples/metrics/prometheus-operator-deployment.yaml -n kafka
kubectl get pods -n kafka
```


**Deploying Prometheus**

```bash
sed -i 's/namespace: .*/namespace: kafka/' ./strimzi-0.41.0/examples/metrics/prometheus-install/prometheus.yaml
kubectl apply -f ./strimzi-0.41.0/examples/metrics/prometheus-additional-properties/prometheus-additional.yaml -n kafka
kubectl apply -f ./strimzi-0.41.0/examples/metrics/prometheus-install/strimzi-pod-monitor.yaml -n kafka
kubectl apply -f ./strimzi-0.41.0/examples/metrics/prometheus-install/prometheus-rules.yaml -n kafka
kubectl apply -f ./strimzi-0.41.0/examples/metrics/prometheus-install/prometheus.yaml -n kafka
kubectl get pods -n kafka
```

**Deploying Grafana**

```bash
kubectl apply -f ./strimzi-0.41.0/examples/metrics/grafana-install/grafana.yaml -n kafka
```




--------------------------------------------------------------------------------
Deploying Connectors in Strimzi
--------------------------------------------------------------------------------


Deploying MySQL

```bash
docker run -it --rm --name mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=debezium -e MYSQL_USER=mysqluser \
  -e MYSQL_PASSWORD=mysqlpw debezium/example-mysql:1.0

docker run -it --rm --name mysqlterm --link mysql --rm mysql:5.7 sh \
  -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" \
  -P"$MYSQL_PORT_3306_TCP_PORT" \
  -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'

```
----------------------------------------------------------
Deploy kafkaConnect in Distributed mode (i,e workers)
----------------------------------------------------------

Kafka Connect image

First download and extract the Debezium MySQL connector archive
  
```bash
curl https://repo1.maven.org/maven2/io/debezium/debezium-connector-mysql/1.0.0.Final/debezium-connector-mysql-1.0.0.Final-plugin.tar.gz \
| tar xvz

cat <<EOF >Dockerfile
FROM quay.io/strimzi/kafka:0.41.0-kafka-3.7.0
USER root:root
RUN mkdir -p /opt/kafka/plugins/debezium
COPY ./debezium-connector-mysql/ /opt/kafka/plugins/debezium/
USER 1001
EOF

# You can use your own dockerhub organization
export DOCKER_ORG=nagabhushanamn
docker build . -t ${DOCKER_ORG}/connect-debezium
docker push ${DOCKER_ORG}/connect-debezium


Secure the database credentials
cat <<EOF > debezium-mysql-credentials.properties
mysql_username: debezium
mysql_password: dbz
EOF
kubectl -n kafka create secret generic my-sql-credentials \
  --from-file=debezium-mysql-credentials.properties
rm debezium-mysql-credentials.properties

# deploy worker
kubectl -n kafka apply -f ./kafka-connect.yaml

# deploy connector
kubectl -n kafka apply -f ./kafka-connector.yaml

# check connectors status
kubectl -n kafka get kctr mysql-inventory-source-connector -o yaml


kubectl -n kafka exec my-cluster-kafka-0 -c kafka -i -t -- bin/kafka-topics.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --list

```

Consumer 
```bash
kubectl -n kafka exec my-cluster-kafka-0 -c kafka -i -t -- \
  bin/kafka-console-consumer.sh \
    --bootstrap-server my-cluster-kafka-bootstrap:9092 \
    --topic dbserver1.inventory.customers 
```

play with mysql database
```sql
SELECT * FROM customers;
UPDATE customers SET first_name='Anne Marie' WHERE id=1004;
```