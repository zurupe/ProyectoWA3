---
applyTo: '**'
---
Tema: Sistema de Seguimiento de Entregas de Pedidos

Construir un sistema web distribuido y seguro que permita a los usuarios registrar, consultar y monitorear pedidos en tiempo real, implementando una arquitectura basada en microservicios, múltiples bases de datos, caché, y control de acceso mediante OAuth2.

La solución deberá contar con:

Angular como frontend.

Microservicio cliente-service con base de datos PostgreSQL para la gestión de clientes.

Microservicio pedido-service con base de datos MySQL para la gestión de pedidos.

Microservicio tracking-service con Redis para ofrecer consultas rápidas de estado de pedidos.

Mecanismo de consistencia eventual entre pedido-service y tracking-service, usando REST para replicar los cambios.

Sistema de autenticación OAuth2, donde los usuarios deben iniciar sesión para acceder a las funcionalidades según su rol (cliente o administrador).



Arquitectura del sistema

Microservicio 1: cliente-service (PostgreSQL)

Registro y gestión de clientes.

Exposición de datos necesarios para vincular pedidos.

Microservicio 2: pedido-service (MySQL)

Registro, actualización y consulta de pedidos.

Después de cada cambio en un pedido, realiza una llamada REST a tracking-service para mantener consistencia eventual.

Protegido por OAuth2 (requiere token de acceso).

Microservicio 3: tracking-service (Redis)

Expone un endpoint rápido de lectura para el estado de los pedidos.

Responde con baja latencia (<0,5s) usando Redis.

Recibe actualizaciones desde pedido-service mediante una REST API protegida.

Verifica token para autorizar actualizaciones.

Microservicio 4: auth-service (OAuth2 Authorization Server)

Autenticación de usuarios y emisión de tokens JWT.

Gestión de roles: ROLE_CLIENTE, ROLE_ADMIN.

Tokens son requeridos para consumir servicios protegidos.

Frontend: Angular

Funcionalidades

Ingreso mediante OAuth2 (password grant, o client credentials para pruebas).

Formulario para crear nuevos pedidos: producto, cliente, dirección.

Vista de pedidos activos por cliente.

Consulta rápida del estado de un pedido por su número (usando tracking-service).

Indicador visual si la información de Redis está desactualizada respecto a la de MySQL.

Protección de rutas mediante guardas de rutas (AuthGuard, RoleGuard).

Seguridad con OAuth2

Todos los microservicios protegidos con Spring Security OAuth2 Resource Server.

auth-service puede estar implementado con un servidor propio.

Angular incluye Interceptor para agregar el token JWT a cada solicitud HTTP.

Consistencia Eventual

Al crear o actualizar un pedido, pedido-service emite una llamada REST a tracking-service para actualizar el estado en Redis.

Si tracking-service no está disponible, se implementa reintento o cola local para garantizar consistencia eventual.

El frontend puede consultar ambas fuentes (MySQL y Redis) para mostrar discrepancias y alertar al usuario.



Nota. Todo el entorno debe estar en docker con docker compose.