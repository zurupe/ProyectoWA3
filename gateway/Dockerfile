# Dockerfile para Spring Cloud Gateway
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/gateway-1.0.0.jar app.jar

EXPOSE 9001

ENTRYPOINT ["java", "-jar", "app.jar"]
