package com.proyecto.trackingservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.proyecto.trackingservice.service.TrackingService;

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
}
