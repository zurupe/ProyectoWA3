

Sistema distribuido para el seguimiento y gestión de pedidos con microservicios, autenticación OAuth2 y frontend Angular.

# Cambios recientes
- Se agregó el endpoint GET `/api/pedidos` para listar todos los pedidos.
- Se implementó el método `obtenerTodosLosPedidos()` en `PedidoService`.
- Se añadieron archivos `.gitignore` y `.dockerignore` para exclusión de archivos temporales y de build.

# Estructura del Proyecto: Sistema de Seguimiento de Pedidos

```
sistema-seguimiento-pedidos/
├── cliente-service/               # Microservicio para gestión de clientes
│   ├── src/                      # Código fuente del microservicio
│   ├── pom.xml                   # Dependencias Maven para Spring Boot
│   └── Dockerfile                # Configuración para contenedor Docker
├── pedido-service/                # Microservicio para gestión de pedidos
│   ├── src/                      # Código fuente del microservicio
│   ├── pom.xml                   # Dependencias Maven para Spring Boot
│   └── Dockerfile                # Configuración para contenedor Docker
├── tracking-service/              # Microservicio para seguimiento en Redis
│   ├── src/                      # Código fuente del microservicio
│   ├── pom.xml                   # Dependencias Maven para Spring Boot
│   └── Dockerfile                # Configuración para contenedor Docker
├── auth-service/                  # Microservicio para autenticación OAuth2
│   ├── src/                      # Código fuente del microservicio
│   ├── pom.xml                   # Dependencias Maven para Spring Boot
│   └── Dockerfile                # Configuración para contenedor Docker
├── frontend/                      # Aplicación Angular
│   ├── src/                      # Código fuente de la aplicación
│   ├── angular.json              # Configuración de Angular
│   └── Dockerfile                # Configuración para contenedor Docker
├── docker-compose.yml             # Orquestación de servicios
├── docs/                          # Documentación del proyecto
│   ├── informe/                   # Carpeta con el informe del proyecto
│   │   ├── portada.md            # Portada del informe
│   │   ├── introduccion.md       # Introducción del proyecto
│   │   ├── objetivos.md          # Objetivos del proyecto
│   │   ├── marco_teorico.md      # Marco teórico
│   │   ├── desarrollo.md         # Desarrollo del proyecto
│   │   ├── requisitos.md         # Requisitos del sistema
│   │   ├── diagramas/            # Diagramas del proyecto
│   │   │   ├── casos_uso.puml    # Diagrama de casos de uso
│   │   │   ├── clases.puml       # Diagrama de clases
│   │   │   ├── secuencia.puml    # Diagrama de secuencia
│   │   │   ├── componentes.puml  # Diagrama de componentes
│   │   ├── arquitectura.md       # Arquitectura del sistema
│   │   ├── consistencia_eventual.md # Explicación de consistencia eventual
│   │   ├── seguridad_autenticacion.md # Seguridad y autenticación
│   │   ├── despliegue.md        # Proceso de despliegue
│   │   ├── pruebas.md           # Pruebas del sistema
│   │   ├── conclusiones.md      # Conclusiones del proyecto
│   │   ├── bibliografia.md      # Bibliografía
│   │   └── anexos.md            # Anexos del informe
│   └── presentacion/             # Carpeta con la presentación
│       └── presentacion.pptx     # Presentación en PowerPoint
└── README.md                      # Instrucciones del proyecto
```

# Uso rápido de la API de pedidos

## Listar todos los pedidos
```bash
curl -X GET http://localhost:8083/api/pedidos -H "Authorization: Bearer <TOKEN>"
```

## Crear un pedido
```bash
curl -X POST http://localhost:8083/api/pedidos \
	-H "Authorization: Bearer <TOKEN>" \
	-H "Content-Type: application/json" \
	-d '{"producto":"Producto X","clienteId":1,"direccion":"Calle 123"}'
```

