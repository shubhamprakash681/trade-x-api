# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY common-lib/pom.xml common-lib/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY market-service/pom.xml market-service/pom.xml
COPY portfolio-service/pom.xml portfolio-service/pom.xml
COPY price-stream-service/pom.xml price-stream-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline
COPY . .
ARG SERVICE
RUN mvn --batch-mode --no-transfer-progress -pl ${SERVICE} -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S tradex && adduser -S tradex -G tradex && apk add --no-cache wget
WORKDIR /app
ARG SERVICE
COPY --from=build /workspace/${SERVICE}/target/*.jar /app/app.jar
USER tradex
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:InitialRAMPercentage=25", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
