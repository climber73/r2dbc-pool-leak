#!/bin/bash

SQL="SELECT 1"

RETRIES=10

until psql --host pg --port 5432 -U postgres -c "$SQL" > /dev/null 2>&1 || [ $RETRIES -eq 0 ]; do
  echo "Waiting for pg server, $((RETRIES--)) remaining attempts..."
  sleep 1
done

for script in /init/*.sql; do
  echo $script
  psql --host pg --port 5432 -U postgres billing -f $script
done
