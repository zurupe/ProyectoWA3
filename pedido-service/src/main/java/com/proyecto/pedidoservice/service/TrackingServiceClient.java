package com.proyecto.pedidoservice.service;

import com.proyecto.pedidoservice.model.Pedido;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Cliente para comunicación con tracking-service con reintentos y cola local
 */
@Component
public class TrackingServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingServiceClient.class);
    
    @Value("${tracking.service.base.url:http://tracking-service:8084}")
    private String trackingServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TrackingQueueService queueService;

    public TrackingServiceClient(TrackingQueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Actualiza el tracking con reintentos automáticos
     */
    @Retryable(
        retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void actualizarTracking(Pedido pedido, String jwtToken) {
        String trackingUrl = trackingServiceBaseUrl + "/api/tracking/" + pedido.getId();
        logger.info("Intentando actualizar tracking para pedido ID: {} con URL: {}", pedido.getId(), trackingUrl);
        logger.debug("JWT Token length: {}", jwtToken != null ? jwtToken.length() : "null");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        
        // Crear payload para el endpoint POST /{pedidoId}
        Map<String, String> payload = new HashMap<>();
        payload.put("estado", pedido.getEstado());
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(trackingUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ Tracking actualizado exitosamente para pedido ID: {}", pedido.getId());
            } else {
                logger.warn("⚠️ Respuesta inesperada del tracking-service: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("❌ Error al actualizar tracking para pedido ID: {} - {}", pedido.getId(), e.getMessage());
            throw e; // Re-lanzar para que @Retryable pueda manejarlo
        }
    }

    /**
     * Método de recuperación cuando fallan todos los reintentos
     */
    @Recover
    public void recover(Exception ex, Pedido pedido, String jwtToken) {
        logger.error("🔄 Todos los reintentos fallaron para pedido ID: {}. Agregando a cola local.", pedido.getId());
        
        // Agregar a cola local para procesamiento posterior
        queueService.agregarACola(pedido, jwtToken);
        
        // Opcional: Notificar a sistema de monitoreo
        logger.warn("📋 Pedido ID: {} agregado a cola local para reintento posterior", pedido.getId());
    }

    /**
     * Método público para forzar reintento desde la cola
     */
    public boolean intentarActualizacionDesdeCola(Pedido pedido, String jwtToken) {
        try {
            actualizarTracking(pedido, jwtToken);
            return true;
        } catch (Exception e) {
            logger.error("❌ Fallo reintento desde cola para pedido ID: {}", pedido.getId());
            return false;
        }
    }
}
