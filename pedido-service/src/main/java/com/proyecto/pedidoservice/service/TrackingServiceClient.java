package com.proyecto.pedidoservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TrackingServiceClient {
    @Value("${tracking.service.url:http://tracking-service:8084/api/tracking/update}")
    private String trackingServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void actualizarTracking(Object payload, String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        HttpEntity<Object> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(trackingServiceUrl, entity, String.class);
            // Puedes manejar la respuesta si es necesario
        } catch (Exception e) {
            // Aquí puedes implementar lógica de reintento o cola local
            System.err.println("Error al actualizar tracking-service: " + e.getMessage());
        }
    }
}
