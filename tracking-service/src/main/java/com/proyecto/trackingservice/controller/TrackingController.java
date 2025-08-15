package com.proyecto.trackingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.proyecto.trackingservice.service.TrackingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    private final TrackingService trackingService;

    @Autowired
    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/{pedidoId}")
    public String getEstadoPedido(@PathVariable String pedidoId) {
        return trackingService.getEstadoPedido(pedidoId);
    }

    @PostMapping("/{pedidoId}")
    public void actualizarEstadoPedido(@PathVariable String pedidoId, @RequestBody String estado) {
        trackingService.actualizarEstadoPedido(pedidoId, estado);
    }

    // Endpoint para consistencia eventual desde pedido-service
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public void actualizarTracking(@RequestBody PedidoDTO pedido, @AuthenticationPrincipal Jwt jwt) {
        // Actualiza el estado en Redis usando el id y estado del pedido
        trackingService.actualizarEstadoPedido(String.valueOf(pedido.getId()), pedido.getEstado());
    }

    // DTO para recibir el pedido
    public static class PedidoDTO {
        private Long id;
        private String estado;

        // getters y setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
