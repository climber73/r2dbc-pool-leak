#!/bin/bash

docker-compose up --detach

./gradlew :clean :test
