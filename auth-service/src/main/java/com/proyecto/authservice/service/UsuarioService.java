package com.proyecto.authservice.service;

import com.proyecto.authservice.entity.Usuario;
import com.proyecto.authservice.entity.Role;
import com.proyecto.authservice.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    /**
     * Crear un nuevo usuario
     */
    public Usuario crearUsuario(Usuario usuario) {
        // Verificar que el username no exista
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El username ya existe: " + usuario.getUsername());
        }

        // Verificar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El email ya existe: " + usuario.getEmail());
        }

        // Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        return usuarioRepository.save(usuario);
    }

    /**
     * Buscar usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Buscar usuario por username
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Buscar usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Listar todos los usuarios
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar usuarios por rol
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarPorRole(Role role) {
        return usuarioRepository.findByRole(role);
    }

    /**
     * Listar usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByEnabledTrue();
    }

    /**
     * Buscar usuarios por texto
     */
    @Transactional(readOnly = true)
    public List<Usuario> buscarUsuarios(String search) {
        return usuarioRepository.searchUsuarios(search);
    }

    /**
     * Actualizar usuario
     */
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Actualizar campos (excepto password si viene vacío)
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setApellido(usuarioActualizado.getApellido());
        usuario.setEmail(usuarioActualizado.getEmail());
        usuario.setRole(usuarioActualizado.getRole());
        usuario.setEnabled(usuarioActualizado.isEnabled());

        return usuarioRepository.save(usuario);
    }

    /**
     * Cambiar contraseña
     */
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Activar/Desactivar usuario
     */
    public void cambiarEstado(Long id, boolean enabled) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setEnabled(enabled);
        usuarioRepository.save(usuario);
    }

    /**
     * Eliminar usuario
     */
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Verificar si existe username
     */
    @Transactional(readOnly = true)
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Verificar si existe email
     */
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Contar usuarios por rol
     */
    @Transactional(readOnly = true)
    public long contarPorRole(Role role) {
        return usuarioRepository.countByRole(role);
    }

    /**
     * Contar usuarios activos
     */
    @Transactional(readOnly = true)
    public long contarActivos() {
        return usuarioRepository.countByEnabledTrue();
    }
}
