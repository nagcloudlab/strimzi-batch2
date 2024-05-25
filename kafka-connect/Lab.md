



kafka connect 
--------------

Start worker-1 in distributed mode
```bash
bin/connect-distributed.sh config/connect-distributed-worker-1.properties
```

Start worker-2 in distributed mode
```bash
bin/connect-distributed.sh config/connect-distributed-worker-2.properties
```

Get list of connectors
```bash
curl -s http://localhost:8083/connectors | jq
```

Create a soure connector  ( e.g file-source-connector)
```bash
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '{
    "name": "file-source-connector",
    "config": {
        "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
        "tasks.max": "1",
        "file": "/home/nag/strimzi-batch2/my-file.txt",
        "topic": "my-topic"
    }
}'
```

Get connector status
```bash
curl -s http://localhost:8083/connectors/file-source-connector/status | jq
```

Delete connector
```bash
curl -X DELETE http://localhost:8083/connectors/file-source-connector
```

Get connector config
```bash
curl -s http://localhost:8083/connectors/file-source-connector | jq
```

Update connector config
```bash
curl -X PUT http://localhost:8083/connectors/file-source-connector/config -H "Content-Type: application/json" -d '{
    "connector.class": "org.apache.kafka.connect.file.FileStreamSourceConnector",
    "tasks.max": "1",
    "file": "/home/nag/strimzi-batch2/my-file.txt",
    "topic": "my-topic"
}'
```

Get connector tasks
```bash
curl -s http://localhost:8083/connectors/file-source-connector/tasks | jq
```

Get connector task status
```bash
curl -s http://localhost:8083/connectors/file-source-connector/tasks/0/status | jq
```

Restart connector task
```bash
curl -X POST http://localhost:8083/connectors/file-source-connector/tasks/0/restart
```

Get connector task config
```bash
curl -s http://localhost:8083/connectors/file-source-connector/tasks/0/config | jq
```


Create a sink connector  ( e.g file-sink-connector)

```bash 
curl -X POST http://localhost:8083/connectors -H "Content-Type: application/json" -d '{
    "name": "file-sink-connector",
    "config": {
        "connector.class": "org.apache.kafka.connect.file.FileStreamSinkConnector",
        "tasks.max": "1",
        "file": "/home/nag/strimzi-batch2/my-file2.txt",
        "topics": "my-topic"
    }
}'
```

Pause connector
```bash
curl -X PUT http://localhost:8083/connectors/file-source-connector/pause
```

Resume connector
```bash
curl -X PUT http://localhost:8083/connectors/file-source-connector/resume
```