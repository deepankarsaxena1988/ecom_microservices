FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/ecomConfigServer-0.0.1-SNAPSHOT.jar ecomConfigServer.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/ecomConfigServer.jar"]
