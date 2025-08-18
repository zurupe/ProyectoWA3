-- Datos iniciales para el auth-service
-- Este archivo se ejecutará automáticamente al iniciar la aplicación

-- Insertar usuario administrador por defecto (password: admin123)
INSERT INTO usuarios (username, password, email, nombre, apellido, role, activo, cuenta_no_expirada, cuenta_no_bloqueada, credenciales_no_expiradas, fecha_creacion, fecha_actualizacion)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTGBr6JdPdkYHOx2wN5QgvDPQ1rW6JUK', 'admin@proyecto.com', 'Administrador', 'Sistema', 'ROLE_ADMIN', true, true, true, true, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- Insertar usuario cliente de prueba (password: cliente123)
INSERT INTO usuarios (username, password, email, nombre, apellido, role, activo, cuenta_no_expirada, cuenta_no_bloqueada, credenciales_no_expiradas, fecha_creacion, fecha_actualizacion)
VALUES ('cliente', '$2a$10$7ZyQZqF.ysXeBcX.OYj5M.6FWYWLZv5ZXo3n9CK6xNqIyN.TdE.gC', 'cliente@proyecto.com', 'Cliente', 'Prueba', 'ROLE_CLIENTE', true, true, true, true, NOW(), NOW())
ON CONFLICT (username) DO NOTHING;