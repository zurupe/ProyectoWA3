package com.proyecto.clienteservice.config;

import com.proyecto.clienteservice.entity.Cliente;
import com.proyecto.clienteservice.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para clientes de prueba
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ClienteRepository clienteRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultClients();
    }

    private void initializeDefaultClients() {
        // Crear clientes de prueba si no existen
        if (clienteRepository.count() == 0) {

            // Cliente 1
            if (!clienteRepository.existsByEmail("juan.perez@email.com")) {
                try {
                    Cliente cliente1 = new Cliente(
                            "Juan", "Pérez", "juan.perez@email.com", "+593987654321",
                            "Av. Amazonas 123", "Quito", "Ecuador");
                    cliente1.setCodigoPostal("170101");
                    clienteRepository.save(cliente1);
                    System.out.println("✅ Cliente creado: Juan Pérez");
                } catch (Exception e) {
                    System.err.println("❌ Error al crear cliente Juan Pérez: " + e.getMessage());
                }
            }

            // Cliente 2
            if (!clienteRepository.existsByEmail("maria.garcia@email.com")) {
                try {
                    Cliente cliente2 = new Cliente(
                            "María", "García", "maria.garcia@email.com", "+593987654322",
                            "Calle Bolívar 456", "Guayaquil", "Ecuador");
                    cliente2.setCodigoPostal("090101");
                    clienteRepository.save(cliente2);
                    System.out.println("✅ Cliente creado: María García");
                } catch (Exception e) {
                    System.err.println("❌ Error al crear cliente María García: " + e.getMessage());
                }
            }

            // Cliente 3
            if (!clienteRepository.existsByEmail("carlos.lopez@email.com")) {
                try {
                    Cliente cliente3 = new Cliente(
                            "Carlos", "López", "carlos.lopez@email.com", "+593987654323",
                            "Av. 10 de Agosto 789", "Cuenca", "Ecuador");
                    cliente3.setCodigoPostal("010101");
                    clienteRepository.save(cliente3);
                    System.out.println("✅ Cliente creado: Carlos López");
                } catch (Exception e) {
                    System.err.println("❌ Error al crear cliente Carlos López: " + e.getMessage());
                }
            }

            // Cliente 4 - Internacional
            if (!clienteRepository.existsByEmail("ana.martinez@email.com")) {
                try {
                    Cliente cliente4 = new Cliente(
                            "Ana", "Martínez", "ana.martinez@email.com", "+34912345678",
                            "Calle Mayor 123", "Madrid", "España");
                    cliente4.setCodigoPostal("28001");
                    clienteRepository.save(cliente4);
                    System.out.println("✅ Cliente creado: Ana Martínez");
                } catch (Exception e) {
                    System.err.println("❌ Error al crear cliente Ana Martínez: " + e.getMessage());
                }
            }

            // Cliente 5 - Desactivado para pruebas
            if (!clienteRepository.existsByEmail("cliente.inactivo@email.com")) {
                try {
                    Cliente cliente5 = new Cliente(
                            "Cliente", "Inactivo", "cliente.inactivo@email.com", "+593987654324",
                            "Dirección temporal", "Quito", "Ecuador");
                    cliente5.setActivo(false);
                    clienteRepository.save(cliente5);
                    System.out.println("✅ Cliente inactivo creado para pruebas");
                } catch (Exception e) {
                    System.err.println("❌ Error al crear cliente inactivo: " + e.getMessage());
                }
            }

            // Mostrar estadísticas
            long totalClientes = clienteRepository.count();
            long clientesActivos = clienteRepository.countByActivoTrue();

            System.out.println("\n📊 Estadísticas de clientes:");
            System.out.println("   - Total clientes: " + totalClientes);
            System.out.println("   - Clientes activos: " + clientesActivos);
            System.out.println("   - Clientes inactivos: " + (totalClientes - clientesActivos));
        }
    }
}
