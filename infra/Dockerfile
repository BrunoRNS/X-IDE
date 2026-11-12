FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY server/pom.xml server/pom.xml
COPY server/src server/src
RUN mvn -B -f server/pom.xml -DskipTests package

FROM nginx:1.27-alpine
RUN apk add --no-cache openjdk17-jre
COPY --from=build /workspace/server/target/server-1.0-SNAPSHOT.jar /opt/xide/server.jar
COPY infra/nginx.conf /etc/nginx/conf.d/default.conf
COPY infra/docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
