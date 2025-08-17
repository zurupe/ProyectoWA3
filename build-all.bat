@echo off
REM Script para compilar todos los microservicios y el api-gateway con mvn clean package -DskipTests

cd /d "%~dp0api-gateway"
echo Compilando api-gateway...
mvn clean package -DskipTests

cd /d "%~dp0auth-service"
echo Compilando auth-service...
mvn clean package -DskipTests

cd /d "%~dp0cliente-service"
echo Compilando cliente-service...
mvn clean package -DskipTests

cd /d "%~dp0pedido-service"
echo Compilando pedido-service...
mvn clean package -DskipTests

cd /d "%~dp0tracking-service"
echo Compilando tracking-service...
mvn clean package -DskipTests

echo Compilación de todos los microservicios y el api-gateway finalizada.
pause
