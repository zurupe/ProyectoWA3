FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="Sistema de Seguimiento de Pedidos"

# Crear directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR
COPY target/oaut-server-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto
EXPOSE 8081

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
