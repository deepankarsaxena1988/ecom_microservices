FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ecomAPIGateWay.jar
ENTRYPOINT ["java","-jar","/ecomAPIGateWay.jar"]