package com.proyecto.pedidoservice.config;

import com.proyecto.pedidoservice.service.TrackingQueueService;
import com.proyecto.pedidoservice.service.TrackingServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para resolver la dependencia circular entre
 * TrackingServiceClient y TrackingQueueService
 */
@Configuration
public class TrackingConfig {

    @Bean
    public TrackingQueueService trackingQueueService() {
        return new TrackingQueueService();
    }

    @Bean
    public TrackingServiceClient trackingServiceClient(TrackingQueueService queueService) {
        TrackingServiceClient client = new TrackingServiceClient(queueService);
        // Configurar la referencia circular
        queueService.setTrackingClient(client);
        return client;
    }
}