package com.proyecto.clienteservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints públicos de salud y información
 */
@RestController
@RequestMapping("/api/clientes/public")
public class HealthController {

    /**
     * Endpoint de salud del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "cliente-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Microservicio de gestión de clientes funcionando correctamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de información del servicio
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Cliente Service");
        response.put("version", "1.0.0");
        response.put("description", "Microservicio para gestión de clientes con PostgreSQL");
        response.put("database", "PostgreSQL");
        response.put("port", 8082);

        // Endpoints principales
        Map<String, String> endpoints = Map.of(
                "clientes", "http://localhost:8082/api/clientes",
                "swagger", "http://localhost:8082/swagger-ui.html",
                "api-docs", "http://localhost:8082/v3/api-docs",
                "health", "http://localhost:8082/api/clientes/public/health");
        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }
}
