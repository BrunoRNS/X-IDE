FROM eclipse-temurin:17-jre
WORKDIR /opt/xide
COPY server/target/x-ide-server.jar /opt/xide/server.jar
EXPOSE 8081
USER 10001:10001
ENTRYPOINT ["java","-jar","/opt/xide/server.jar"]
