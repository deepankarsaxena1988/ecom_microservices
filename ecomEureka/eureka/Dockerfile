FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ecomEurekaServer.jar
ENTRYPOINT ["java","-jar","/ecomEurekaServer.jar"]