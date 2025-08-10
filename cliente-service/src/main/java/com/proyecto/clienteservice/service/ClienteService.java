package com.proyecto.clienteservice.service;

import com.proyecto.clienteservice.dto.ClienteRequest;
import com.proyecto.clienteservice.dto.ClienteResponse;
import com.proyecto.clienteservice.entity.Cliente;
import com.proyecto.clienteservice.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clientes
 */
@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Crear un nuevo cliente
     */
    public ClienteResponse crearCliente(ClienteRequest request) {
        // Validar que el email no exista
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con este email: " + request.getEmail());
        }

        Cliente cliente = new Cliente(
                request.getNombre(),
                request.getApellido(),
                request.getEmail(),
                request.getTelefono(),
                request.getDireccion(),
                request.getCiudad(),
                request.getPais());
        cliente.setCodigoPostal(request.getCodigoPostal());

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return convertirAResponse(clienteGuardado);
    }

    /**
     * Obtener cliente por ID
     */
    @Transactional(readOnly = true)
    public Optional<ClienteResponse> obtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .map(this::convertirAResponse);
    }

    /**
     * Obtener cliente por email
     */
    @Transactional(readOnly = true)
    public Optional<ClienteResponse> obtenerClientePorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .map(this::convertirAResponse);
    }

    /**
     * Obtener todos los clientes
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerTodosLosClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes activos
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerClientesActivos() {
        return clienteRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Buscar clientes por término de búsqueda
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarClientes(String busqueda) {
        return clienteRepository.buscarClientes(busqueda)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes por ciudad
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerClientesPorCiudad(String ciudad) {
        return clienteRepository.findByCiudadIgnoreCase(ciudad)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener clientes por país
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> obtenerClientesPorPais(String pais) {
        return clienteRepository.findByPaisIgnoreCase(pais)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar cliente
     */
    public ClienteResponse actualizarCliente(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        // Validar email único (excluyendo el cliente actual)
        if (clienteRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new IllegalArgumentException("Ya existe otro cliente con este email: " + request.getEmail());
        }

        // Actualizar campos
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setCiudad(request.getCiudad());
        cliente.setPais(request.getPais());
        cliente.setCodigoPostal(request.getCodigoPostal());

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return convertirAResponse(clienteActualizado);
    }

    /**
     * Cambiar estado de cliente (activar/desactivar)
     */
    public ClienteResponse cambiarEstadoCliente(Long id, Boolean activo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        cliente.setActivo(activo);
        Cliente clienteActualizado = clienteRepository.save(cliente);
        return convertirAResponse(clienteActualizado);
    }

    /**
     * Eliminar cliente (eliminación física)
     */
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con ID: " + id);
        }
        clienteRepository.deleteById(id);
    }

    /**
     * Verificar si existe un cliente por email
     */
    @Transactional(readOnly = true)
    public boolean existeClientePorEmail(String email) {
        return clienteRepository.existsByEmail(email);
    }

    /**
     * Contar clientes activos
     */
    @Transactional(readOnly = true)
    public long contarClientesActivos() {
        return clienteRepository.countByActivoTrue();
    }

    /**
     * Contar clientes por país
     */
    @Transactional(readOnly = true)
    public long contarClientesPorPais(String pais) {
        return clienteRepository.countByPaisIgnoreCase(pais);
    }

    /**
     * Convertir entidad Cliente a ClienteResponse
     */
    private ClienteResponse convertirAResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getCiudad(),
                cliente.getPais(),
                cliente.getCodigoPostal(),
                cliente.getActivo(),
                cliente.getFechaCreacion(),
                cliente.getFechaActualizacion());
    }
}
