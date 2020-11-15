#!/bin/bash

SQL="CREATE DATABASE IF NOT EXISTS billing"

RETRIES=10

until cockroach sql --host cockroach --execute "$SQL" --insecure > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do
  echo "Waiting for cockroach server, $((RETRIES--)) remaining attempts..."
  sleep 1
done

for script in /init/*.sql; do
  echo $script
  cockroach sql --url postgresql://cockroach/billing --insecure < $script
done
