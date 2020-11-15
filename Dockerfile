FROM openjdk:11-jre-slim
LABEL MAINTAINER="crpt.ru"
ARG VERSION

ENV JAVA_OPTS="-noverify --add-opens=java.base/java.nio=ALL-UNNAMED -Duser.language=en -Duser.timezone=UTC -XX:MaxRAMPercentage=70 -Dfile.encoding=UTF-8"

EXPOSE 8080

ENTRYPOINT /opt/billing/bin/billing

ADD build/distributions/billing-$VERSION.tar /opt
RUN mv /opt/billing-* /opt/billing
WORKDIR /opt/billing