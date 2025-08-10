FROM openjdk:17-jdk-slim

# Información del mantenedor
LABEL maintainer="Sistema de Seguimiento de Pedidos"

# Crear directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR
COPY target/pedido-service-1.0.0.jar app.jar

# Exponer el puerto
EXPOSE 8083

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8083

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
