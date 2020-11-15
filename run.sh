#!/bin/bash

./gradlew clean bootJar

docker-compose up --detach --build

echo "waiting for the billing application starting (5 sec)..."
sleep 5

# loading the app (less than a minute)
docker run --rm -v $(pwd)/tank:/var/loadtest -it direvius/yandex-tank

echo "waiting for the application is idle (5 sec)..."
sleep 5

# get r2dbc_pool_acquired_connections gauge from the app
# it turns out that it is not 0
curl -s localhost:8080/actuator/prometheus | grep r2dbc_pool_acquired_connections | grep -v "^#"
