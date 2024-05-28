



```bash
kubectl apply -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/kafka/kafka-persistent.yaml -n kafka
```

Cruise Control
---------------

Kafka re-balance full
```bash
kubectl apply -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/cruise-control/kafka-rebalance-full.yaml -n kafka
```

kafka re-balance add broker
```bash
kubectl apply -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/cruise-control/kafka-rebalance-add-brokers.yaml -n kafka
```

kafka re-balance remove broker
```bash
kubectl apply -f /home/nag/strimzi-batch2/strimzi-0.41.0/examples/cruise-control/kafka-rebalance-remove-brokers.yaml -n kafka
```

Check the status of the rebalance
```bash
kubectl get kafkarebalance  -n kafka -w
```

View the status of the rebalance
```bash
kubectl describe kafkarebalance my-rebalance -n kafka
```

Annotate to approve the rebalance
```bash
kubectl annotate kafkarebalance my-rebalance strimzi.io/rebalance=approve -n kafka
```

