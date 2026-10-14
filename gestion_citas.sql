-- PASO 1: conectado a postgres, ejecutar SOLAMENTE esta linea,
-- fuera de una transaccion (autocommit). No volver a crear si ya existe.
CREATE DATABASE gestion_citas;

-- PASO 2: abrir otra Query Tool conectada a gestion_citas.
-- Ejecutar desde BEGIN hasta COMMIT. No ejecutarlo en postgres.
BEGIN;
CREATE TABLE IF NOT EXISTS public.cliente (
    id SERIAL PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL CHECK (btrim(nombres) <> ''),
    apellidos VARCHAR(80) NOT NULL CHECK (btrim(apellidos) <> ''),
    telefono VARCHAR(20) NOT NULL CHECK (btrim(telefono) <> ''),
    correo VARCHAR(120) NOT NULL UNIQUE CHECK (btrim(correo) <> ''),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS public.cita_tecnica (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tipo_servicio VARCHAR(80) NOT NULL CHECK (btrim(tipo_servicio) <> ''),
    descripcion VARCHAR(255) NOT NULL CHECK (btrim(descripcion) <> ''),
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id)
        REFERENCES public.cliente(id) ON DELETE RESTRICT,
    CONSTRAINT chk_cita_estado CHECK (estado IN ('Pendiente','En proceso','Finalizada','Cancelada'))
);
COMMIT;

-- PASO 3: comprobar la base y las dos tablas; no se crean datos simulados.
SELECT current_database();
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public' AND table_name IN ('cliente','cita_tecnica');
