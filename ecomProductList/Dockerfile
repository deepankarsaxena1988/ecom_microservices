FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ecomProductList.jar
ENTRYPOINT ["java","-jar","/ecomProductList.jar"]