package com.proyecto.trackingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyecto.trackingservice.service.TrackingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);
    private final TrackingService trackingService;

    @Autowired
    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Obtiene el estado actual de un pedido
     */
    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<?> getEstadoPedido(@PathVariable String pedidoId, @AuthenticationPrincipal Jwt jwt) {
        try {
            TrackingService.TrackingInfo info = trackingService.getEstadoPedido(pedidoId);
            if (info == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("❌ Error al obtener estado de pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener estado del pedido");
        }
    }

    /**
     * Actualiza el estado de un pedido (endpoint directo)
     */
    @PostMapping("/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstadoPedido(@PathVariable String pedidoId, @RequestBody EstadoRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            trackingService.actualizarEstadoPedido(pedidoId, request.getEstado());
            logger.info("✅ Estado actualizado via endpoint directo para pedido ID: {} por usuario: {}", pedidoId, jwt.getSubject());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error al actualizar estado de pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al actualizar estado del pedido");
        }
    }

    /**
     * Endpoint para consistencia eventual desde pedido-service
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<?> actualizarTracking(@RequestBody PedidoDTO pedido, @AuthenticationPrincipal Jwt jwt) {
        try {
            trackingService.actualizarEstadoPedido(String.valueOf(pedido.getId()), pedido.getEstado());
            logger.info("🔄 Tracking actualizado via consistencia eventual para pedido ID: {} desde pedido-service", pedido.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Estado inválido en consistencia eventual para pedido ID: {} - {}", pedido.getId(), e.getMessage());
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error en consistencia eventual para pedido ID: {} - {}", pedido.getId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Error al actualizar tracking");
        }
    }

    /**
     * Obtiene el historial de cambios de un pedido
     */
    @GetMapping("/{pedidoId}/historial")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public ResponseEntity<List<TrackingService.HistorialEntry>> getHistorialPedido(@PathVariable String pedidoId, @AuthenticationPrincipal Jwt jwt) {
        try {
            List<TrackingService.HistorialEntry> historial = trackingService.getHistorialPedido(pedidoId);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            logger.error("❌ Error al obtener historial de pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene estadísticas de tracking (solo admin)
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEstadisticas(@AuthenticationPrincipal Jwt jwt) {
        try {
            Map<String, Object> stats = trackingService.getEstadisticas();
            logger.info("📊 Estadísticas de tracking consultadas por usuario: {}", jwt.getSubject());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Error al obtener estadísticas - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para sincronizar un pedido específico (cuando no existe tracking)
     */
    @PostMapping("/sync/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sincronizarPedido(@PathVariable String pedidoId, @RequestBody EstadoRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            trackingService.actualizarEstadoPedido(pedidoId, request.getEstado());
            logger.info("🔄 Tracking sincronizado para pedido ID: {} con estado: {} por usuario: {}", 
                       pedidoId, request.getEstado(), jwt.getSubject());
            return ResponseEntity.ok(Map.of("message", "Tracking sincronizado correctamente", "pedidoId", pedidoId, "estado", request.getEstado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error al sincronizar tracking para pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al sincronizar tracking");
        }
    }

    /**
     * Endpoint para sincronizar desde pedido-service hacia tracking
     */
    @PutMapping("/sync/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sincronizarDesdePedido(@PathVariable String pedidoId, @RequestBody EstadoRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            trackingService.actualizarEstadoPedido(pedidoId, request.getEstado());
            logger.info("🔄 Tracking sincronizado desde pedido-service para pedido ID: {} con estado: {} por usuario: {}", 
                       pedidoId, request.getEstado(), jwt.getSubject());
            return ResponseEntity.ok(Map.of("message", "Tracking sincronizado desde pedido", "pedidoId", pedidoId, "estado", request.getEstado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error al sincronizar tracking desde pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al sincronizar tracking");
        }
    }

    /**
     * Endpoint para crear tracking desde datos de pedido existente
     */
    @PostMapping("/create-from-pedido/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearTrackingDesdePedido(@PathVariable String pedidoId, @RequestBody EstadoRequest request, @AuthenticationPrincipal Jwt jwt) {
        try {
            // Primero verificar si ya existe tracking
            TrackingService.TrackingInfo existente = trackingService.getEstadoPedido(pedidoId);
            if (existente != null) {
                return ResponseEntity.badRequest().body("Ya existe tracking para el pedido " + pedidoId);
            }
            
            trackingService.actualizarEstadoPedido(pedidoId, request.getEstado());
            logger.info("✅ Tracking creado desde pedido para pedido ID: {} con estado: {} por usuario: {}", 
                       pedidoId, request.getEstado(), jwt.getSubject());
            return ResponseEntity.ok(Map.of("message", "Tracking creado desde pedido", "pedidoId", pedidoId, "estado", request.getEstado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Estado inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error al crear tracking desde pedido ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al crear tracking desde pedido");
        }
    }

    /**
     * Health check específico para tracking
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "tracking-service",
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("❌ Error en health check - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sincronización manual hacia pedido-service
     */
    @PostMapping("/sync-to-pedido/{pedidoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sincronizarHaciaPedido(@PathVariable String pedidoId, @AuthenticationPrincipal Jwt jwt) {
        try {
            String jwtToken = jwt.getTokenValue();
            Map<String, Object> resultado = trackingService.sincronizarManualmentePedido(pedidoId, jwtToken);
            
            if (resultado.containsKey("error")) {
                return ResponseEntity.badRequest().body(resultado);
            }
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("❌ Error en sincronización manual hacia pedido para ID: {} - {}", pedidoId, e.getMessage());
            return ResponseEntity.internalServerError().body("Error en sincronización manual");
        }
    }

    // DTOs
    public static class PedidoDTO {
        private Long id;
        private String estado;

        public PedidoDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }

    public static class EstadoRequest {
        private String estado;

        public EstadoRequest() {}

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
