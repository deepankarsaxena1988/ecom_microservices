FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/eureka-0.0.1-SNAPSHOT.jar ecomEureka.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "/app/ecomEureka.jar"]