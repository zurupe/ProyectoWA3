-- Script de inicialización para PostgreSQL
-- Crear base de datos y usuario para cliente-service

-- Crear base de datos para clientes si no existe
CREATE DATABASE cliente_db;

-- Crear usuario para cliente-service si no existe
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cliente_user') THEN
        CREATE USER cliente_user WITH PASSWORD 'cliente_pass';
    END IF;
END
$$;

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE cliente_db TO cliente_user;

-- Conectar a la base de datos cliente_db
\c cliente_db;

-- Otorgar permisos en el esquema public
GRANT ALL ON SCHEMA public TO cliente_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cliente_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cliente_user;

-- Asegurar que el usuario pueda crear tablas
ALTER USER cliente_user CREATEDB;
