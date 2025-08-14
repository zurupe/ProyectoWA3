package com.proyecto.authservice.controller;

import com.proyecto.authservice.dto.RegistroRequest;
import com.proyecto.authservice.dto.UsuarioResponse;
import com.proyecto.authservice.entity.Role;
import com.proyecto.authservice.entity.Usuario;
import com.proyecto.authservice.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    /**
     * Registro de nuevos usuarios
     */
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroRequest request) {
        try {
            Usuario usuario = new Usuario(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getNombre(),
                    request.getApellido(),
                    request.getRole() != null ? request.getRole() : Role.ROLE_CLIENTE);

            Usuario usuarioCreado = usuarioService.crearUsuario(usuario);
            UsuarioResponse response = convertirAResponse(usuarioCreado);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Obtener información del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
            }

            String username = authentication.getName();
            Usuario usuario = usuarioService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            UsuarioResponse response = convertirAResponse(usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener información del usuario: " + e.getMessage());
        }
    }

    /**
     * Listar todos los usuarios (solo ADMIN)
     */
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Verificar que el usuario tenga rol ADMIN
            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if (!esAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado. Se requiere rol de administrador.");
            }

            List<Usuario> usuarios = usuarioService.listarTodos();
            List<UsuarioResponse> response = usuarios.stream()
                    .map(this::convertirAResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar usuarios: " + e.getMessage());
        }
    }

    /**
     * Buscar usuario por ID
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Verificar que el usuario sea admin o esté consultando su propia información
            Usuario usuarioActual = usuarioService.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));

            boolean esAdmin = usuarioActual.getRole() == Role.ROLE_ADMIN;
            boolean esMismoUsuario = usuarioActual.getId().equals(id);

            if (!esAdmin && !esMismoUsuario) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado. Solo puedes consultar tu propia información.");
            }

            Usuario usuario = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            UsuarioResponse response = convertirAResponse(usuario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuario: " + e.getMessage());
        }
    }

    /**
     * Verificar disponibilidad de username
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<?> verificarUsername(@PathVariable String username) {
        try {
            boolean existe = usuarioService.existeUsername(username);
            return ResponseEntity.ok().body(new CheckResponse("username", !existe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar username: " + e.getMessage());
        }
    }

    /**
     * Verificar disponibilidad de email
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> verificarEmail(@PathVariable String email) {
        try {
            boolean existe = usuarioService.existeEmail(email);
            return ResponseEntity.ok().body(new CheckResponse("email", !existe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar email: " + e.getMessage());
        }
    }

    /**
     * Login de usuario y generación de JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        Usuario usuario = usuarioService.buscarPorUsername(username).orElse(null);
        if (usuario == null || !usuario.isEnabled() || !passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(username)
                .claim("role", usuario.getRole().name())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return ResponseEntity.ok(Map.of("access_token", token));
    }

    /**
     * Convertir Usuario a UsuarioResponse
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRole(),
                usuario.isEnabled(),
                usuario.getFechaCreacion(),
                usuario.getFechaActualizacion());
    }

    // Clases de respuesta internas
    private static class CheckResponse {
        private final String field;
        private final boolean available;

        public CheckResponse(String field, boolean available) {
            this.field = field;
            this.available = available;
        }

        public String getField() {
            return field;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}
