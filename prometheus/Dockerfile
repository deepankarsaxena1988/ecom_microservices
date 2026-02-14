ARG ARCH="amd64"
ARG OS="linux"
#FROM openjdk:17
#ENV PATH="/usr/lib/jvm/java-17-openjdk/bin:${PATH}"
FROM prom/prometheus:latest
ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} ecom_prometheus.jar
COPY src/main/resources/prometheus.yml  /etc/prometheus/prometheus.yml
WORKDIR /prometheus
#RUN sudo chown -R nobody:nobody /etc/prometheus /prometheus && chmod g+w /prometheus
#USER       nobody
EXPOSE     9090
ENTRYPOINT [ "/bin/prometheus" ]
CMD        [ "--config.file=/etc/prometheus/prometheus.yml"]
#ENTRYPOINT ["java","-jar","/ecom_prometheus.jar"]