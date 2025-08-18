package com.proyecto.trackingservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingService.class);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Value("${pedido.service.base.url:http://pedido-service:8083}")
    private String pedidoServiceBaseUrl;
    
    // Estados válidos para validación (consistentes con frontend y pedido-service)
    private static final Set<String> ESTADOS_VALIDOS = Set.of(
        "PENDIENTE", "PROCESANDO", "ENVIADO", "ENTREGADO", "CANCELADO"
    );

    @Autowired
    public TrackingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    /**
     * Obtiene el estado actual del pedido
     */
    public TrackingInfo getEstadoPedido(String pedidoId) {
        try {
            String data = redisTemplate.opsForValue().get("tracking:" + pedidoId);
            if (data == null) {
                logger.warn("📭 No se encontró tracking para pedido ID: {}", pedidoId);
                return null;
            }
            
            TrackingInfo info = objectMapper.readValue(data, TrackingInfo.class);
            logger.info("📋 Estado consultado para pedido ID: {} - Estado: {}", pedidoId, info.getEstado());
            return info;
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error al deserializar tracking para pedido ID: {} - {}", pedidoId, e.getMessage());
            return null;
        }
    }

    /**
     * Actualiza el estado del pedido con timestamp y logging
     */
    public void actualizarEstadoPedido(String pedidoId, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            logger.error("❌ Estado inválido '{}' para pedido ID: {}. Estados válidos: {}", 
                        nuevoEstado, pedidoId, ESTADOS_VALIDOS);
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }

        try {
            String estadoAnterior = null;
            
            // Obtener estado anterior si existe
            String dataAnterior = redisTemplate.opsForValue().get("tracking:" + pedidoId);
            if (dataAnterior != null) {
                try {
                    TrackingInfo infoAnterior = objectMapper.readValue(dataAnterior, TrackingInfo.class);
                    estadoAnterior = infoAnterior.getEstado();
                } catch (JsonProcessingException e) {
                    logger.warn("⚠️ Error al leer estado anterior para pedido ID: {}", pedidoId);
                }
            }

            // Crear nueva información de tracking
            TrackingInfo nuevoTracking = new TrackingInfo(
                pedidoId, 
                nuevoEstado, 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            // Almacenar en Redis
            String data = objectMapper.writeValueAsString(nuevoTracking);
            redisTemplate.opsForValue().set("tracking:" + pedidoId, data);
            
            // Agregar al historial
            agregarAlHistorial(pedidoId, nuevoEstado, estadoAnterior);
            
            // Notificar al pedido-service para consistencia eventual
            notificarPedidoService(pedidoId, nuevoEstado);
            
            // Log detallado
            if (estadoAnterior != null) {
                logger.info("🔄 Estado actualizado para pedido ID: {} - {} → {} ({})", 
                           pedidoId, estadoAnterior, nuevoEstado, 
                           LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                logger.info("✅ Estado inicial creado para pedido ID: {} - {} ({})", 
                           pedidoId, nuevoEstado, 
                           LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error al serializar tracking para pedido ID: {} - {}", pedidoId, e.getMessage());
            throw new RuntimeException("Error al actualizar tracking", e);
        }
    }

    /**
     * Obtiene el historial de cambios de estado de un pedido
     */
    public List<HistorialEntry> getHistorialPedido(String pedidoId) {
        try {
            List<String> historial = redisTemplate.opsForList().range("historial:" + pedidoId, 0, -1);
            List<HistorialEntry> entries = new ArrayList<>();
            
            if (historial != null) {
                for (String entry : historial) {
                    try {
                        HistorialEntry historialEntry = objectMapper.readValue(entry, HistorialEntry.class);
                        entries.add(historialEntry);
                    } catch (JsonProcessingException e) {
                        logger.warn("⚠️ Error al deserializar entrada de historial para pedido ID: {}", pedidoId);
                    }
                }
            }
            
            logger.info("📚 Historial consultado para pedido ID: {} - {} entradas", pedidoId, entries.size());
            return entries;
            
        } catch (Exception e) {
            logger.error("❌ Error al obtener historial para pedido ID: {} - {}", pedidoId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene estadísticas de tracking
     */
    public Map<String, Object> getEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Set<String> keys = redisTemplate.keys("tracking:*");
            stats.put("totalPedidosTrackeados", keys != null ? keys.size() : 0);
            
            Map<String, Integer> estadosCounts = new HashMap<>();
            if (keys != null) {
                for (String key : keys) {
                    try {
                        String data = redisTemplate.opsForValue().get(key);
                        if (data != null) {
                            TrackingInfo info = objectMapper.readValue(data, TrackingInfo.class);
                            estadosCounts.merge(info.getEstado(), 1, Integer::sum);
                        }
                    } catch (JsonProcessingException e) {
                        logger.warn("⚠️ Error al procesar estadística para key: {}", key);
                    }
                }
            }
            
            stats.put("estadosDistribucion", estadosCounts);
            stats.put("fechaConsulta", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            logger.info("📊 Estadísticas generadas - Total pedidos: {}", stats.get("totalPedidosTrackeados"));
            
        } catch (Exception e) {
            logger.error("❌ Error al generar estadísticas - {}", e.getMessage());
            stats.put("error", "Error al generar estadísticas");
        }
        
        return stats;
    }

    /**
     * Agrega entrada al historial de cambios
     */
    private void agregarAlHistorial(String pedidoId, String nuevoEstado, String estadoAnterior) {
        try {
            HistorialEntry entry = new HistorialEntry(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                estadoAnterior,
                nuevoEstado
            );
            
            String entryJson = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForList().leftPush("historial:" + pedidoId, entryJson);
            
            // Mantener solo las últimas 50 entradas del historial
            redisTemplate.opsForList().trim("historial:" + pedidoId, 0, 49);
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error al agregar al historial para pedido ID: {} - {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Notifica al pedido-service sobre cambios de estado para consistencia eventual
     */
    private void notificarPedidoService(String pedidoId, String nuevoEstado) {
        try {
            // Obtener JWT del contexto de seguridad
            String jwtToken = null;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Jwt) {
                jwtToken = ((Jwt) principal).getTokenValue();
            }
            
            if (jwtToken == null) {
                logger.warn("⚠️ No se pudo obtener JWT token para sincronizar pedido ID: {}", pedidoId);
                return;
            }
            
            // Preparar headers y body
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(jwtToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = Map.of("estado", nuevoEstado);
            org.springframework.http.HttpEntity<Map<String, String>> entity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // Llamar al endpoint de sincronización del pedido-service
            String url = pedidoServiceBaseUrl + "/api/pedidos/" + pedidoId + "/sync-from-tracking";
            restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, String.class);
            
            logger.info("🔄 Pedido-service notificado exitosamente para pedido ID: {} con estado: {}", pedidoId, nuevoEstado);
            
        } catch (RestClientException e) {
            logger.warn("⚠️ Error al notificar pedido-service para pedido ID: {} - {}. La sincronización será manejada posteriormente.", 
                       pedidoId, e.getMessage());
            // En una implementación más robusta, aquí se podría agregar a una cola de reintentos
        } catch (Exception e) {
            logger.error("❌ Error inesperado al notificar pedido-service para pedido ID: {} - {}", pedidoId, e.getMessage());
        }
    }

    /**
     * Método para sincronización manual en caso de fallos automáticos
     */
    public Map<String, Object> sincronizarManualmentePedido(String pedidoId, String jwtToken) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Obtener estado actual del tracking
            TrackingInfo tracking = getEstadoPedido(pedidoId);
            if (tracking == null) {
                resultado.put("error", "No existe tracking para el pedido " + pedidoId);
                return resultado;
            }
            
            // Preparar headers y body
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(jwtToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            Map<String, String> requestBody = Map.of("estado", tracking.getEstado());
            org.springframework.http.HttpEntity<Map<String, String>> entity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
            
            // Llamar al endpoint de sincronización del pedido-service
            String url = pedidoServiceBaseUrl + "/api/pedidos/" + pedidoId + "/sync-from-tracking";
            restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, entity, String.class);
            
            resultado.put("message", "Sincronización manual exitosa");
            resultado.put("pedidoId", pedidoId);
            resultado.put("estado", tracking.getEstado());
            resultado.put("fechaSincronizacion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            logger.info("✅ Sincronización manual exitosa para pedido ID: {} con estado: {}", pedidoId, tracking.getEstado());
            
        } catch (RestClientException e) {
            resultado.put("error", "Error de conectividad con pedido-service: " + e.getMessage());
            logger.error("❌ Error en sincronización manual para pedido ID: {} - {}", pedidoId, e.getMessage());
        } catch (Exception e) {
            resultado.put("error", "Error inesperado en sincronización: " + e.getMessage());
            logger.error("❌ Error inesperado en sincronización manual para pedido ID: {} - {}", pedidoId, e.getMessage());
        }
        
        return resultado;
    }

    // Clases internas para estructurar los datos
    public static class TrackingInfo {
        private String pedidoId;
        private String estado;
        private String fechaActualizacion;

        public TrackingInfo() {}

        public TrackingInfo(String pedidoId, String estado, String fechaActualizacion) {
            this.pedidoId = pedidoId;
            this.estado = estado;
            this.fechaActualizacion = fechaActualizacion;
        }

        // Getters y setters
        public String getPedidoId() { return pedidoId; }
        public void setPedidoId(String pedidoId) { this.pedidoId = pedidoId; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getFechaActualizacion() { return fechaActualizacion; }
        public void setFechaActualizacion(String fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    }

    public static class HistorialEntry {
        private String timestamp;
        private String estadoAnterior;
        private String estadoNuevo;

        public HistorialEntry() {}

        public HistorialEntry(String timestamp, String estadoAnterior, String estadoNuevo) {
            this.timestamp = timestamp;
            this.estadoAnterior = estadoAnterior;
            this.estadoNuevo = estadoNuevo;
        }

        // Getters y setters
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getEstadoAnterior() { return estadoAnterior; }
        public void setEstadoAnterior(String estadoAnterior) { this.estadoAnterior = estadoAnterior; }
        public String getEstadoNuevo() { return estadoNuevo; }
        public void setEstadoNuevo(String estadoNuevo) { this.estadoNuevo = estadoNuevo; }
    }
}
