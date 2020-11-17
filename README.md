# Simple project to reproduce possible r2dbc-pool leak

## Run test
```shell script
./run.sh
```

## Description

What's required to reproduce the leak:
1) PostgreSQL (provided as docker container)
2) Transactions with serializable isolation level
3) Relatively heavy load which involve concurrent (with the same account id) DB requests
4) The client should retry 40001 errors (serialization_failure)
5) The client should close HTTP connection by timeout (to get the cancel signal sent to the reactive chains)