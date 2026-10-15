-- =====================================================
-- PASO 1: ejecutar solamente esta instrucción
-- conectado a la base postgres, con autocommit.
-- =====================================================

CREATE DATABASE gestion_citas;


-- =====================================================
-- PASO 2: abrir Query Tool conectado a gestion_citas.
-- Ejecutar todo lo que sigue.
-- =====================================================

BEGIN;

-- Tabla de clientes
CREATE TABLE public.cliente (
                                id SERIAL PRIMARY KEY,

                                nombres VARCHAR(80) NOT NULL,
                                apellidos VARCHAR(80) NOT NULL,
                                telefono VARCHAR(20) NOT NULL,
                                correo VARCHAR(120) NOT NULL UNIQUE,
                                activo BOOLEAN NOT NULL DEFAULT TRUE,

                                CONSTRAINT chk_cliente_nombres
                                    CHECK (btrim(nombres) <> ''),

                                CONSTRAINT chk_cliente_apellidos
                                    CHECK (btrim(apellidos) <> ''),

                                CONSTRAINT chk_cliente_telefono
                                    CHECK (btrim(telefono) <> ''),

                                CONSTRAINT chk_cliente_correo
                                    CHECK (btrim(correo) <> '')
);


-- Tabla de citas técnicas
CREATE TABLE public.cita_tecnica (
                                     id SERIAL PRIMARY KEY,

                                     cliente_id INTEGER NOT NULL,
                                     fecha DATE NOT NULL,
                                     hora TIME NOT NULL,
                                     tipo_servicio VARCHAR(80) NOT NULL,
                                     descripcion VARCHAR(255) NOT NULL,
                                     estado VARCHAR(20) NOT NULL,

    -- Impide eliminar clientes que tengan citas asociadas.
                                     CONSTRAINT fk_cita_cliente
                                         FOREIGN KEY (cliente_id)
                                             REFERENCES public.cliente(id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT chk_cita_servicio
                                         CHECK (btrim(tipo_servicio) <> ''),

                                     CONSTRAINT chk_cita_descripcion
                                         CHECK (btrim(descripcion) <> ''),

                                     CONSTRAINT chk_cita_estado
                                         CHECK (
                                             estado IN (
                                                        'Pendiente',
                                                        'En proceso',
                                                        'Finalizada',
                                                        'Cancelada'
                                                 )
                                             ),

    -- Impide repetir el horario de un mismo cliente.
    -- Se aplica tanto al insertar como al actualizar.
                                     CONSTRAINT uq_cita_cliente_fecha_hora
                                         UNIQUE (cliente_id, fecha, hora)
);

COMMIT;


-- =====================================================
-- COMPROBACIONES
-- =====================================================

-- Debe mostrar gestion_citas.
SELECT current_database() AS base_actual;

-- Debe mostrar cliente y cita_tecnica.
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('cliente', 'cita_tecnica')
ORDER BY table_name;
