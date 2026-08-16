# ============================================================
# Build
# ============================================================

FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /workspace

COPY . .

ARG SERVICE

RUN mvn -B -DskipTests package \
    -pl ${SERVICE} \
    -am


# ============================================================
# Java runtime
# ============================================================

FROM eclipse-temurin:25-jre-alpine AS java-runtime

ARG SERVICE

WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder \
    /workspace/${SERVICE}/target/*.jar \
    /app/app.jar

RUN chown -R spring:spring /app

USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


# ============================================================
# Nginx
# ============================================================

FROM nginx:1.29-alpine AS nginx

COPY nginx/conf/nginx.conf \
     /etc/nginx/nginx.conf

COPY nginx/conf/conf.d/ \
     /etc/nginx/conf.d/

RUN mkdir -p /var/www/certbot

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]