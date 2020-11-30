#!/bin/bash

docker-compose up --detach

./gradlew --info :clean :test
