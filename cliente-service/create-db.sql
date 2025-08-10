-- Crear base de datos cliente_db
-- Ejecutar en PostgreSQL como superusuario

-- Crear usuario cliente_user si no existe
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cliente_user') THEN
        CREATE USER cliente_user WITH PASSWORD 'cliente_pass';
    END IF;
END
$$;

-- Crear base de datos cliente_db si no existe
SELECT 'CREATE DATABASE cliente_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cliente_db')\gexec

-- Otorgar privilegios
GRANT ALL PRIVILEGES ON DATABASE cliente_db TO cliente_user;

-- Mensaje de confirmación
\echo 'Base de datos cliente_db creada y configurada exitosamente'
