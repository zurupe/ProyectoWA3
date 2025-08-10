package com.proyecto.authservice.repository;

import com.proyecto.authservice.entity.Usuario;
import com.proyecto.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Buscar usuario por username
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Buscar usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verificar si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);

    /**
     * Verificar si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);

    /**
     * Buscar usuarios por rol
     */
    List<Usuario> findByRole(Role role);

    /**
     * Buscar usuarios activos
     */
    List<Usuario> findByEnabledTrue();

    /**
     * Buscar usuarios por nombre o apellido (búsqueda parcial)
     */
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Usuario> searchUsuarios(@Param("search") String search);

    /**
     * Contar usuarios por rol
     */
    long countByRole(Role role);

    /**
     * Contar usuarios activos
     */
    long countByEnabledTrue();
}
