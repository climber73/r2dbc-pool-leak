FROM openjdk:11-jre-slim

EXPOSE 8080

ENTRYPOINT java -jar /opt/r2dbc-pool-leak.jar

ADD ./build/libs/r2dbc-pool-leak.jar /opt