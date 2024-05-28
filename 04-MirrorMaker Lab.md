

```bash
kubectl delete -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/mirror-maker/kafka-source.yaml -n kafka
```

```bash
kubectl delete -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/mirror-maker/kafka-target.yaml -n kafka
```

```bash
kubectl delete -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/mirror-maker/kafka-mirror-maker-2.yaml -n kafka
```

```bash
kubectl exec -it cluster-a-kafka-0 -- bin/kafka-topics.sh --create --topic my-topic --partitions 1 --replication-factor 1 --bootstrap-server kafka-source-kafka-bootstrap:9092