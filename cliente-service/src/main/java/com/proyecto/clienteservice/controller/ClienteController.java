package com.proyecto.clienteservice.controller;

import com.proyecto.clienteservice.dto.ClienteRequest;
import com.proyecto.clienteservice.dto.ClienteResponse;
import com.proyecto.clienteservice.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de clientes
 */
@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Clientes", description = "API para gestión de clientes")
@SecurityRequirement(name = "Bearer Authentication")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Crear nuevo cliente
     */
    @PostMapping
    @Operation(summary = "Crear nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "409", description = "Email ya existe")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> crearCliente(@Valid @RequestBody ClienteRequest request) {
        try {
            ClienteResponse cliente = clienteService.crearCliente(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Obtener todos los clientes
     */
    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Obtiene la lista de todos los clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerTodosLosClientes() {
        try {
            List<ClienteResponse> clientes = clienteService.obtenerTodosLosClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener clientes: " + e.getMessage());
        }
    }

    /**
     * Obtener clientes activos
     */
    @GetMapping("/activos")
    @Operation(summary = "Obtener clientes activos", description = "Obtiene la lista de clientes activos")
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos obtenida exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerClientesActivos() {
        try {
            List<ClienteResponse> clientes = clienteService.obtenerClientesActivos();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener clientes activos: " + e.getMessage());
        }
    }

    /**
     * Obtener cliente por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Obtiene un cliente específico por su ID")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerClientePorId(@Parameter(description = "ID del cliente") @PathVariable Long id) {
        try {
            return clienteService.obtenerClientePorId(id)
                    .map(cliente -> ResponseEntity.ok(cliente))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener cliente: " + e.getMessage());
        }
    }

    /**
     * Obtener cliente por email
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener cliente por email", description = "Obtiene un cliente específico por su email")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerClientePorEmail(
            @Parameter(description = "Email del cliente") @PathVariable String email) {
        try {
            return clienteService.obtenerClientePorEmail(email)
                    .map(cliente -> ResponseEntity.ok(cliente))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener cliente por email: " + e.getMessage());
        }
    }

    /**
     * Buscar clientes
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes", description = "Busca clientes por nombre, apellido o email")
    @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> buscarClientes(
            @Parameter(description = "Término de búsqueda") @RequestParam String busqueda) {
        try {
            List<ClienteResponse> clientes = clienteService.buscarClientes(busqueda);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en la búsqueda: " + e.getMessage());
        }
    }

    /**
     * Obtener clientes por ciudad
     */
    @GetMapping("/ciudad/{ciudad}")
    @Operation(summary = "Obtener clientes por ciudad", description = "Obtiene clientes de una ciudad específica")
    @ApiResponse(responseCode = "200", description = "Clientes encontrados por ciudad")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerClientesPorCiudad(
            @Parameter(description = "Nombre de la ciudad") @PathVariable String ciudad) {
        try {
            List<ClienteResponse> clientes = clienteService.obtenerClientesPorCiudad(ciudad);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener clientes por ciudad: " + e.getMessage());
        }
    }

    /**
     * Obtener clientes por país
     */
    @GetMapping("/pais/{pais}")
    @Operation(summary = "Obtener clientes por país", description = "Obtiene clientes de un país específico")
    @ApiResponse(responseCode = "200", description = "Clientes encontrados por país")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> obtenerClientesPorPais(
            @Parameter(description = "Nombre del país") @PathVariable String pais) {
        try {
            List<ClienteResponse> clientes = clienteService.obtenerClientesPorPais(pais);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener clientes por país: " + e.getMessage());
        }
    }

    /**
     * Actualizar cliente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarCliente(@Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        try {
            ClienteResponse cliente = clienteService.actualizarCliente(id, request);
            return ResponseEntity.ok(cliente);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar cliente: " + e.getMessage());
        }
    }

    /**
     * Cambiar estado de cliente
     */
    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de cliente", description = "Activa o desactiva un cliente")
    @ApiResponse(responseCode = "200", description = "Estado de cliente cambiado exitosamente")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstadoCliente(@Parameter(description = "ID del cliente") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam Boolean activo) {
        try {
            ClienteResponse cliente = clienteService.cambiarEstadoCliente(id, activo);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar estado: " + e.getMessage());
        }
    }

    /**
     * Eliminar cliente
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente del sistema")
    @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarCliente(@Parameter(description = "ID del cliente") @PathVariable Long id) {
        try {
            clienteService.eliminarCliente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar cliente: " + e.getMessage());
        }
    }

    /**
     * Verificar si existe email
     */
    @GetMapping("/verificar-email/{email}")
    @Operation(summary = "Verificar email", description = "Verifica si existe un cliente con el email dado")
    @ApiResponse(responseCode = "200", description = "Verificación completada")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<?> verificarEmail(@Parameter(description = "Email a verificar") @PathVariable String email) {
        try {
            boolean existe = clienteService.existeClientePorEmail(email);
            return ResponseEntity.ok().body(new EmailExisteResponse(email, !existe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar email: " + e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de clientes
     */
    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas", description = "Obtiene estadísticas de clientes")
    @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            long clientesActivos = clienteService.contarClientesActivos();
            return ResponseEntity.ok().body(new EstadisticasResponse(clientesActivos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    // Clases internas para respuestas específicas
    public static class EmailExisteResponse {
        private String email;
        private boolean disponible;

        public EmailExisteResponse(String email, boolean disponible) {
            this.email = email;
            this.disponible = disponible;
        }

        public String getEmail() {
            return email;
        }

        public boolean isDisponible() {
            return disponible;
        }
    }

    public static class EstadisticasResponse {
        private long clientesActivos;

        public EstadisticasResponse(long clientesActivos) {
            this.clientesActivos = clientesActivos;
        }

        public long getClientesActivos() {
            return clientesActivos;
        }
    }
}
