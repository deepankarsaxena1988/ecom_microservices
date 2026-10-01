@echo off
cd /d "C:\Work\ecom\ecom_service_branches\feature-observability-grafana-prometheus-docker\ecom_microservices\ecomAccounts"
mvn spring-boot:run -Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
