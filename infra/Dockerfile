FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml pom.xml

COPY client/pom.xml client/pom.xml

COPY common/pom.xml common/pom.xml
COPY common/src common/src

COPY server/pom.xml server/pom.xml
COPY server/src server/src

RUN mvn -B -U -f pom.xml -pl server -am -DskipTests package



FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        openjdk-17-jre-headless \
        nginx \
        ca-certificates \
        openssl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/xide


COPY --from=build /workspace/server/target/server-1.0-SNAPSHOT.jar /opt/xide/server.jar


COPY infra/nginx/nginx.conf /etc/nginx/conf.d/default.conf


COPY infra/nginx/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && rm -f /etc/nginx/sites-enabled/default

EXPOSE 8443

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
