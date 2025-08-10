package com.proyecto.authservice.config;

import com.proyecto.authservice.entity.Role;
import com.proyecto.authservice.entity.Usuario;
import com.proyecto.authservice.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Crear usuario administrador por defecto si no existe
        if (!usuarioService.existeUsername("admin")) {
            Usuario admin = new Usuario(
                    "admin",
                    "admin123", // Se encriptará automáticamente en el servicio
                    "admin@proyecto.com",
                    "Administrador",
                    "Sistema",
                    Role.ROLE_ADMIN);

            try {
                usuarioService.crearUsuario(admin);
                System.out.println("✅ Usuario administrador creado: admin/admin123");
            } catch (Exception e) {
                System.err.println("❌ Error al crear usuario administrador: " + e.getMessage());
            }
        }

        // Crear usuario cliente de prueba si no existe
        if (!usuarioService.existeUsername("cliente")) {
            Usuario cliente = new Usuario(
                    "cliente",
                    "cliente123",
                    "cliente@proyecto.com",
                    "Cliente",
                    "Prueba",
                    Role.ROLE_CLIENTE);

            try {
                usuarioService.crearUsuario(cliente);
                System.out.println("✅ Usuario cliente creado: cliente/cliente123");
            } catch (Exception e) {
                System.err.println("❌ Error al crear usuario cliente: " + e.getMessage());
            }
        }

        // Mostrar estadísticas
        long totalUsuarios = usuarioService.listarTodos().size();
        long adminCount = usuarioService.contarPorRole(Role.ROLE_ADMIN);
        long clienteCount = usuarioService.contarPorRole(Role.ROLE_CLIENTE);

        System.out.println("📊 Estadísticas de usuarios:");
        System.out.println("   - Total usuarios: " + totalUsuarios);
        System.out.println("   - Administradores: " + adminCount);
        System.out.println("   - Clientes: " + clienteCount);
    }
}
