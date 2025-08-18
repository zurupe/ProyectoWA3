package com.proyecto.pedidoservice.service;

import com.proyecto.pedidoservice.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de cola local para garantizar consistencia eventual
 * cuando tracking-service no está disponible
 */
@Service
public class TrackingQueueService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingQueueService.class);
    
    // Cola thread-safe para almacenar pedidos pendientes de sincronización
    private final ConcurrentLinkedQueue<TrackingQueueItem> colaLocal = new ConcurrentLinkedQueue<>();
    
    private final AtomicInteger intentosFallidos = new AtomicInteger(0);
    private final AtomicInteger itemsProcesados = new AtomicInteger(0);
    
    private TrackingServiceClient trackingClient;

    // Inyección circular evitada con setter
    public void setTrackingClient(TrackingServiceClient trackingClient) {
        this.trackingClient = trackingClient;
    }

    /**
     * Agregar pedido a la cola local
     */
    public void agregarACola(Pedido pedido, String jwtToken) {
        TrackingQueueItem item = new TrackingQueueItem(pedido, jwtToken, LocalDateTime.now());
        colaLocal.offer(item);
        
        logger.info("📋 Pedido ID: {} agregado a cola local. Tamaño cola: {}", 
                   pedido.getId(), colaLocal.size());
    }

    /**
     * Procesar cola local cada 30 segundos
     */
    @Scheduled(fixedRate = 30000) // 30 segundos
    @Async
    public void procesarColaLocal() {
        if (colaLocal.isEmpty()) {
            return;
        }

        logger.info("🔄 Procesando cola local. Items pendientes: {}", colaLocal.size());
        
        int procesados = 0;
        int fallidos = 0;
        
        // Procesar hasta 10 items por batch para evitar sobrecarga
        for (int i = 0; i < 10 && !colaLocal.isEmpty(); i++) {
            TrackingQueueItem item = colaLocal.poll();
            
            if (item == null) break;
            
            // Verificar si el item no es muy antiguo (máximo 1 hora)
            if (item.getFechaCreacion().isBefore(LocalDateTime.now().minusHours(1))) {
                logger.warn("⏰ Item muy antiguo descartado. Pedido ID: {}", item.getPedido().getId());
                continue;
            }
            
            try {
                boolean exito = trackingClient.intentarActualizacionDesdeCola(
                    item.getPedido(), item.getJwtToken());
                
                if (exito) {
                    procesados++;
                    itemsProcesados.incrementAndGet();
                    logger.info("✅ Pedido ID: {} sincronizado desde cola local", item.getPedido().getId());
                } else {
                    // Volver a agregar a la cola para siguiente intento
                    colaLocal.offer(item);
                    fallidos++;
                    intentosFallidos.incrementAndGet();
                }
            } catch (Exception e) {
                logger.error("❌ Error procesando pedido ID: {} desde cola", item.getPedido().getId());
                // Volver a agregar a la cola para siguiente intento
                colaLocal.offer(item);
                fallidos++;
            }
        }
        
        if (procesados > 0 || fallidos > 0) {
            logger.info("📊 Batch procesado - Exitosos: {}, Fallidos: {}, Pendientes: {}", 
                       procesados, fallidos, colaLocal.size());
        }
    }

    /**
     * Obtener estadísticas de la cola
     */
    public ColaEstadisticas obtenerEstadisticas() {
        return new ColaEstadisticas(
            colaLocal.size(),
            itemsProcesados.get(),
            intentosFallidos.get()
        );
    }

    /**
     * Limpiar cola (para testing o mantenimiento)
     */
    public void limpiarCola() {
        int size = colaLocal.size();
        colaLocal.clear();
        logger.warn("🧹 Cola local limpiada. Items eliminados: {}", size);
    }

    /**
     * Item de la cola local
     */
    private static class TrackingQueueItem {
        private final Pedido pedido;
        private final String jwtToken;
        private final LocalDateTime fechaCreacion;

        public TrackingQueueItem(Pedido pedido, String jwtToken, LocalDateTime fechaCreacion) {
            this.pedido = pedido;
            this.jwtToken = jwtToken;
            this.fechaCreacion = fechaCreacion;
        }

        public Pedido getPedido() { return pedido; }
        public String getJwtToken() { return jwtToken; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    }

    /**
     * Estadísticas de la cola
     */
    public static class ColaEstadisticas {
        private final int itemsPendientes;
        private final int totalProcesados;
        private final int totalFallidos;

        public ColaEstadisticas(int itemsPendientes, int totalProcesados, int totalFallidos) {
            this.itemsPendientes = itemsPendientes;
            this.totalProcesados = totalProcesados;
            this.totalFallidos = totalFallidos;
        }

        public int getItemsPendientes() { return itemsPendientes; }
        public int getTotalProcesados() { return totalProcesados; }
        public int getTotalFallidos() { return totalFallidos; }
    }
}