package com.proyecto.pedidoservice.controller;

import com.proyecto.pedidoservice.model.Pedido;
import com.proyecto.pedidoservice.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestión de pedidos
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {
    private final PedidoService pedidoService;

    @Autowired
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Crear nuevo pedido
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> crearPedido(@Valid @RequestBody Pedido pedido) {
        try {
            Pedido nuevoPedido = pedidoService.crearPedido(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedido);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear pedido: " + e.getMessage());
        }
    }

    /**
     * Obtener pedidos por cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerPedidosPorCliente(@PathVariable Long clienteId) {
        try {
            List<Pedido> pedidos = pedidoService.obtenerPedidosPorCliente(clienteId);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pedidos: " + e.getMessage());
        }
    }

    /**
     * Obtener pedidos por estado
     */
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerPedidosPorEstado(@PathVariable String estado) {
        try {
            List<Pedido> pedidos = pedidoService.obtenerPedidosPorEstado(estado);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pedidos por estado: " + e.getMessage());
        }
    }

    /**
     * Obtener pedido por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerPedidoPorId(@PathVariable Long id) {
        try {
            Optional<Pedido> pedido = pedidoService.obtenerPedidoPorId(id);
            return pedido.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pedido: " + e.getMessage());
        }
    }

    /**
     * Actualizar estado del pedido (solo admin)
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarEstadoPedido(@PathVariable Long id, @RequestParam String estado) {
        try {
            Pedido actualizado = pedidoService.actualizarEstadoPedido(id, estado);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar estado: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los pedidos (solo admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerTodosLosPedidos() {
        try {
            List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener pedidos: " + e.getMessage());
        }
    }

    /**
     * Comparar discrepancias entre MySQL y Redis (solo admin)
     */
    @GetMapping("/compare-tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> compararDiscrepancias() {
        try {
            Map<String, Object> discrepancias = pedidoService.compararTrackingDiscrepancias();
            return ResponseEntity.ok(discrepancias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al comparar discrepancias: " + e.getMessage());
        }
    }
}
