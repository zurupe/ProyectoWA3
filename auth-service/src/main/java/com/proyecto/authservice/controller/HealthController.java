package com.proyecto.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para verificar el estado del servicio de autenticación
 */
@RestController
@RequestMapping("/api/auth/public")
public class HealthController {

    /**
     * Endpoint público para verificar que el servicio está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Servicio de autenticación OAuth2 funcionando correctamente");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint público para obtener información del servidor OAuth2
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "OAuth2 Authorization Server");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
                "authorization", "http://localhost:8081/oauth2/authorize",
                "token", "http://localhost:8081/oauth2/token",
                "jwks", "http://localhost:8081/oauth2/jwks",
                "userinfo", "http://localhost:8081/userinfo",
                "registration", "http://localhost:8081/api/auth/register"));
        response.put("supported_grants", new String[] {
                "authorization_code",
                "refresh_token",
                "client_credentials"
        });

        return ResponseEntity.ok(response);
    }
}
