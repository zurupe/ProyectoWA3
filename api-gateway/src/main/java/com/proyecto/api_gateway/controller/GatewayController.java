package com.proyecto.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints informativos del API Gateway
 */
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Autowired
    private RouteLocator routeLocator;

    /**
     * Health check del gateway
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "api-gateway");
        health.put("timestamp", LocalDateTime.now());
        health.put("description", "API Gateway para sistema de seguimiento de pedidos");
        
        return Mono.just(ResponseEntity.ok(health));
    }

    /**
     * Información sobre las rutas configuradas
     */
    @GetMapping("/routes")
    public Mono<ResponseEntity<Map<String, Object>>> routes() {
        return routeLocator.getRoutes()
                .collectList()
                .map(routes -> {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("totalRoutes", routes.size());
                    routeInfo.put("timestamp", LocalDateTime.now());
                    
                    Map<String, String> routeDetails = new HashMap<>();
                    routes.forEach(route -> {
                        String routeId = route.getId();
                        String uri = route.getUri().toString();
                        routeDetails.put(routeId, uri);
                    });
                    
                    routeInfo.put("routes", routeDetails);
                    return ResponseEntity.ok(routeInfo);
                });
    }

    /**
     * Información general del gateway
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "API Gateway");
        info.put("version", "1.0.0");
        info.put("description", "Gateway unificado para microservicios de seguimiento de pedidos");
        info.put("author", "Sistema de Seguimiento de Entregas");
        info.put("timestamp", LocalDateTime.now());
        
        Map<String, String> services = new HashMap<>();
        services.put("auth-service", "http://localhost:8081 - Autenticación OAuth2");
        services.put("cliente-service", "http://localhost:8082 - Gestión de clientes");
        services.put("pedido-service", "http://localhost:8083 - Gestión de pedidos");
        services.put("tracking-service", "http://localhost:8084 - Tracking de pedidos");
        
        info.put("services", services);
        
        return Mono.just(ResponseEntity.ok(info));
    }
}