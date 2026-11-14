# Multi-arch image: linux/amd64, linux/arm64, linux/386, linux/arm/v7.
# The Maven build stage is pinned to $BUILDPLATFORM: Java bytecode is
# architecture-independent, so one native build serves every target platform.
# The runtime base is debian:bookworm-slim because it is the only common base
# shipping a JRE 17 for the 32-bit targets (Alpine openjdk17 has no i386 or
# armv7 packages).
FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY client/pom.xml client/pom.xml
COPY common/pom.xml common/pom.xml
COPY common/src common/src
COPY server/pom.xml server/pom.xml
COPY server/src server/src
RUN mvn -B -f pom.xml -pl server -am -DskipTests package

FROM debian:bookworm-slim
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless nginx \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /opt/xide
COPY --from=build /workspace/server/target/server-1.0-SNAPSHOT-all.jar /opt/xide/server.jar
COPY infra/nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY infra/nginx/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh \
    && rm -f /etc/nginx/sites-enabled/default
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
