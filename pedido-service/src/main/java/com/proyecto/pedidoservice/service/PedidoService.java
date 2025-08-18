package com.proyecto.pedidoservice.service;

import com.proyecto.pedidoservice.model.Pedido;
import com.proyecto.pedidoservice.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final TrackingServiceClient trackingServiceClient;
    private final RestTemplate restTemplate;
    
    @Value("${cliente.service.url:http://cliente-service:8082}")
    private String clienteServiceUrl;
    
    @Value("${tracking.service.base.url:http://tracking-service:8084}")
    private String trackingServiceBaseUrl;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, TrackingServiceClient trackingServiceClient) {
        this.pedidoRepository = pedidoRepository;
        this.trackingServiceClient = trackingServiceClient;
        this.restTemplate = new RestTemplate();
    }

    public Pedido crearPedido(Pedido pedido) {
        // Validar que el cliente existe
        if (!validarClienteExiste(pedido.getClienteId())) {
            throw new IllegalArgumentException("Cliente con ID " + pedido.getClienteId() + " no existe");
        }
        
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
            try {
                // Intento de sincronización con reintentos automáticos
                trackingServiceClient.actualizarTracking(pedido, jwtToken);
            } catch (Exception e) {
                // Los reintentos y cola local se manejan automáticamente
                // por @Retryable y @Recover en TrackingServiceClient
                System.err.println("Error en sincronización con tracking-service para pedido ID: " + pedido.getId());
            }
        } else {
            System.err.println("⚠️ No se pudo obtener JWT token para sincronizar pedido ID: " + pedido.getId());
        }
    }
    
    /**
     * Valida si un cliente existe consultando el cliente-service
     */
    private boolean validarClienteExiste(Long clienteId) {
        try {
            // Obtener JWT del contexto de seguridad para la llamada
            String jwtToken = null;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Jwt) {
                jwtToken = ((Jwt) principal).getTokenValue();
            }
            
            if (jwtToken == null) {
                System.err.println("⚠️ No se pudo obtener JWT token para validar cliente ID: " + clienteId);
                return false; // Fallar de manera segura
            }
            
            // Crear headers con JWT
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(jwtToken);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // Llamar al cliente-service para verificar existencia
            String url = clienteServiceUrl + "/api/clientes/" + clienteId;
            org.springframework.http.ResponseEntity<String> response = 
                restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            boolean existe = response.getStatusCode().is2xxSuccessful();
            System.out.println(existe ? 
                "✅ Cliente ID " + clienteId + " validado exitosamente" : 
                "❌ Cliente ID " + clienteId + " no encontrado");
            
            return existe;
            
        } catch (RestClientException e) {
            System.err.println("❌ Error al validar cliente ID " + clienteId + ": " + e.getMessage());
            // En caso de error de conectividad, podrías decidir si fallar o continuar
            // Para este caso, vamos a fallar de manera segura
            return false;
        }
    }
    
    /**
     * Compara discrepancias entre MySQL y Redis
     */
    public Map<String, Object> compararTrackingDiscrepancias() {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> discrepancias = new ArrayList<>();
        
        try {
            // Obtener todos los pedidos de MySQL
            List<Pedido> pedidosMySQL = pedidoRepository.findAll();
            
            // Obtener JWT token para las consultas
            String jwtToken = null;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Jwt) {
                jwtToken = ((Jwt) principal).getTokenValue();
            }
            
            if (jwtToken == null) {
                resultado.put("error", "No se pudo obtener JWT token para comparación");
                return resultado;
            }
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(jwtToken);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            int totalPedidos = pedidosMySQL.size();
            int discrepanciasEncontradas = 0;
            int errorConsultas = 0;
            
            for (Pedido pedido : pedidosMySQL) {
                try {
                    // Consultar estado en Redis
                    String trackingUrl = trackingServiceBaseUrl + "/api/tracking/" + pedido.getId();
                    org.springframework.http.ResponseEntity<Map> trackingResponse = 
                        restTemplate.exchange(trackingUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
                    
                    if (trackingResponse.getStatusCode().is2xxSuccessful() && trackingResponse.getBody() != null) {
                        Map<String, Object> trackingData = trackingResponse.getBody();
                        String estadoRedis = (String) trackingData.get("estado");
                        String estadoMySQL = pedido.getEstado();
                        
                        if (!Objects.equals(estadoMySQL, estadoRedis)) {
                            discrepanciasEncontradas++;
                            Map<String, Object> discrepancia = new HashMap<>();
                            discrepancia.put("pedidoId", pedido.getId());
                            discrepancia.put("estadoMySQL", estadoMySQL);
                            discrepancia.put("estadoRedis", estadoRedis);
                            discrepancia.put("producto", pedido.getProducto());
                            discrepancia.put("fechaActualizacion", pedido.getFechaActualizacion());
                            discrepancias.add(discrepancia);
                        }
                    } else {
                        // Pedido existe en MySQL pero no en Redis
                        discrepanciasEncontradas++;
                        Map<String, Object> discrepancia = new HashMap<>();
                        discrepancia.put("pedidoId", pedido.getId());
                        discrepancia.put("estadoMySQL", pedido.getEstado());
                        discrepancia.put("estadoRedis", "NO_ENCONTRADO");
                        discrepancia.put("producto", pedido.getProducto());
                        discrepancia.put("fechaActualizacion", pedido.getFechaActualizacion());
                        discrepancias.add(discrepancia);
                    }
                    
                } catch (RestClientException e) {
                    errorConsultas++;
                    System.err.println("❌ Error al consultar tracking para pedido ID " + pedido.getId() + ": " + e.getMessage());
                }
            }
            
            // Preparar resultado
            resultado.put("totalPedidos", totalPedidos);
            resultado.put("discrepanciasEncontradas", discrepanciasEncontradas);
            resultado.put("errorConsultas", errorConsultas);
            resultado.put("consistenciaPercentage", totalPedidos > 0 ? 
                ((totalPedidos - discrepanciasEncontradas) * 100.0 / totalPedidos) : 100.0);
            resultado.put("discrepancias", discrepancias);
            resultado.put("fechaComparacion", LocalDateTime.now());
            resultado.put("estado", discrepanciasEncontradas == 0 ? "CONSISTENTE" : "INCONSISTENTE");
            
            System.out.println(discrepanciasEncontradas == 0 ? 
                "✅ Comparación completada - Datos consistentes" : 
                "⚠️ Comparación completada - " + discrepanciasEncontradas + " discrepancias encontradas");
            
        } catch (Exception e) {
            resultado.put("error", "Error durante la comparación: " + e.getMessage());
            System.err.println("❌ Error en comparación de discrepancias: " + e.getMessage());
        }
        
        return resultado;
    }
}
