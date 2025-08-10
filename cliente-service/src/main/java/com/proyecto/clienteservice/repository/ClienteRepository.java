package com.proyecto.clienteservice.repository;

import com.proyecto.clienteservice.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de clientes
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Buscar cliente por email
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verificar si existe un cliente con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Buscar clientes activos
     */
    List<Cliente> findByActivoTrue();

    /**
     * Buscar clientes por ciudad
     */
    List<Cliente> findByCiudadIgnoreCase(String ciudad);

    /**
     * Buscar clientes por país
     */
    List<Cliente> findByPaisIgnoreCase(String pais);

    /**
     * Buscar clientes por nombre o apellido (búsqueda flexible)
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Cliente> buscarClientes(@Param("busqueda") String busqueda);

    /**
     * Buscar clientes por nombre completo
     */
    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(CONCAT(c.nombre, ' ', c.apellido)) LIKE LOWER(CONCAT('%', :nombreCompleto, '%'))")
    List<Cliente> findByNombreCompleto(@Param("nombreCompleto") String nombreCompleto);

    /**
     * Contar clientes activos
     */
    long countByActivoTrue();

    /**
     * Contar clientes por país
     */
    long countByPaisIgnoreCase(String pais);

    /**
     * Buscar clientes por rango de ID (útil para paginación personalizada)
     */
    List<Cliente> findByIdBetween(Long idInicio, Long idFin);

    /**
     * Buscar clientes activos por ciudad
     */
    List<Cliente> findByActivoTrueAndCiudadIgnoreCase(String ciudad);

    /**
     * Verificar si existe un email diferente al cliente actual (para validación en
     * actualización)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Cliente c WHERE c.email = :email AND c.id != :clienteId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("clienteId") Long clienteId);
}
