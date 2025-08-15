package com.proyecto.pedidoservice.service;

import com.proyecto.pedidoservice.model.Pedido;
import com.proyecto.pedidoservice.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final TrackingServiceClient trackingServiceClient;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, TrackingServiceClient trackingServiceClient) {
        this.pedidoRepository = pedidoRepository;
        this.trackingServiceClient = trackingServiceClient;
    }

    public Pedido crearPedido(Pedido pedido) {
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        Pedido saved = pedidoRepository.save(pedido);
        // Consistencia eventual: notificar a tracking-service
        notificarTrackingService(saved);
        return saved;
    }

    public List<Pedido> obtenerPedidosPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Optional<Pedido> obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido actualizarEstadoPedido(Long id, String nuevoEstado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(id);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setEstado(nuevoEstado);
            pedido.setFechaActualizacion(LocalDateTime.now());
            Pedido actualizado = pedidoRepository.save(pedido);
            // Consistencia eventual: notificar a tracking-service
            notificarTrackingService(actualizado);
            return actualizado;
        }
        throw new RuntimeException("Pedido no encontrado");
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAll();
    }

    private void notificarTrackingService(Pedido pedido) {
        // Obtener JWT del contexto de seguridad
        String jwtToken = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            jwtToken = ((Jwt) principal).getTokenValue();
        } else {
            // Alternativamente, obtener del request si está en el header
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwtToken = authHeader.substring(7);
                }
            }
        }
        if (jwtToken != null) {
            trackingServiceClient.actualizarTracking(pedido, jwtToken);
        }
    }
}
