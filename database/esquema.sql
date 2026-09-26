-- PlatoYa — Segunda entrega: Diseño y módulos
-- Motor: PostgreSQL
-- Diseño inicial pendiente de aprobación del tutor.
-- Ejecutar únicamente sobre una base de datos vacía.
-- No incluye implementación de la aplicación ni datos reales.

BEGIN;

CREATE TABLE usuarios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nombre_usuario VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(10) NOT NULL,
    fecha_baja TIMESTAMPTZ,

    CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT ck_usuarios_rol
        CHECK (rol IN ('ADMIN', 'MOZO', 'COCINA', 'BARRA'))
);

CREATE TABLE mesas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero INTEGER NOT NULL,
    capacidad INTEGER NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'LIBRE',
    fecha_baja TIMESTAMPTZ,

    CONSTRAINT uq_mesas_numero UNIQUE (numero),
    CONSTRAINT ck_mesas_numero CHECK (numero > 0),
    CONSTRAINT ck_mesas_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_mesas_estado
        CHECK (estado IN ('LIBRE', 'OCUPADA'))
);

CREATE TABLE categorias (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    destino VARCHAR(10) NOT NULL,
    fecha_baja TIMESTAMPTZ,

    CONSTRAINT uq_categorias_nombre UNIQUE (nombre),
    CONSTRAINT ck_categorias_destino
        CHECK (destino IN ('COCINA', 'BARRA'))
);

CREATE TABLE productos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categoria_id BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    precio NUMERIC(12,2) NOT NULL,
    destino VARCHAR(10),
    fecha_baja TIMESTAMPTZ,

    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias(id) ON DELETE RESTRICT,

    CONSTRAINT ck_productos_precio CHECK (precio >= 0),
    CONSTRAINT ck_productos_destino
        CHECK (destino IS NULL OR destino IN ('COCINA', 'BARRA'))
);

CREATE TABLE cuentas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mesa_id BIGINT NOT NULL,
    mozo_apertura_id BIGINT NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'ABIERTA',
    fecha_apertura TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cuenta_pedida TIMESTAMPTZ,
    fecha_cierre TIMESTAMPTZ,
    anulada_por_id BIGINT,
    motivo_anulacion VARCHAR(255),

    CONSTRAINT fk_cuentas_mesa
        FOREIGN KEY (mesa_id)
        REFERENCES mesas(id) ON DELETE RESTRICT,

    CONSTRAINT fk_cuentas_mozo
        FOREIGN KEY (mozo_apertura_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    CONSTRAINT fk_cuentas_anulada_por
        FOREIGN KEY (anulada_por_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    CONSTRAINT ck_cuentas_estado
        CHECK (
            estado IN ('ABIERTA', 'CUENTA_PEDIDA', 'PAGADA', 'ANULADA')
        ),

    CONSTRAINT ck_cuentas_cierre
        CHECK (
            (
                estado IN ('ABIERTA', 'CUENTA_PEDIDA')
                AND fecha_cierre IS NULL
            )
            OR
            (
                estado IN ('PAGADA', 'ANULADA')
                AND fecha_cierre IS NOT NULL
            )
        ),

    CONSTRAINT ck_cuentas_solicitud
        CHECK (
            estado <> 'CUENTA_PEDIDA'
            OR fecha_cuenta_pedida IS NOT NULL
        ),

    CONSTRAINT ck_cuentas_anulacion
        CHECK (
            (
                estado = 'ANULADA'
                AND anulada_por_id IS NOT NULL
                AND motivo_anulacion IS NOT NULL
                AND LENGTH(TRIM(motivo_anulacion)) > 0
            )
            OR
            (
                estado <> 'ANULADA'
                AND anulada_por_id IS NULL
                AND motivo_anulacion IS NULL
            )
        ),

    CONSTRAINT ck_cuentas_orden_fechas
        CHECK (
            (
                fecha_cuenta_pedida IS NULL
                OR fecha_cuenta_pedida >= fecha_apertura
            )
            AND
            (
                fecha_cierre IS NULL
                OR fecha_cierre >= fecha_apertura
            )
            AND
            (
                fecha_cierre IS NULL
                OR fecha_cuenta_pedida IS NULL
                OR fecha_cierre >= fecha_cuenta_pedida
            )
        )
);

CREATE TABLE comandas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    mozo_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    fecha_envio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comandas_cuenta
        FOREIGN KEY (cuenta_id)
        REFERENCES cuentas(id) ON DELETE RESTRICT,

    CONSTRAINT fk_comandas_mozo
        FOREIGN KEY (mozo_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    CONSTRAINT uq_comandas_cuenta_numero UNIQUE (cuenta_id, numero),
    CONSTRAINT ck_comandas_numero CHECK (numero > 0)
);

CREATE TABLE items_comanda (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    comanda_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    nombre_producto VARCHAR(120) NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL,
    destino VARCHAR(10) NOT NULL,
    cantidad INTEGER NOT NULL,
    observacion VARCHAR(255),
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE',
    fecha_preparacion TIMESTAMPTZ,
    fecha_listo TIMESTAMPTZ,
    fecha_entrega TIMESTAMPTZ,
    fecha_anulacion TIMESTAMPTZ,
    anulado_por_id BIGINT,
    motivo_anulacion VARCHAR(255),
    merma BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_items_comanda
        FOREIGN KEY (comanda_id)
        REFERENCES comandas(id) ON DELETE RESTRICT,

    CONSTRAINT fk_items_producto
        FOREIGN KEY (producto_id)
        REFERENCES productos(id) ON DELETE RESTRICT,

    CONSTRAINT fk_items_anulado_por
        FOREIGN KEY (anulado_por_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    CONSTRAINT ck_items_precio CHECK (precio_unitario >= 0),
    CONSTRAINT ck_items_cantidad CHECK (cantidad > 0),

    CONSTRAINT ck_items_destino
        CHECK (destino IN ('COCINA', 'BARRA')),

    CONSTRAINT ck_items_estado
        CHECK (
            estado IN (
                'PENDIENTE',
                'EN_PREPARACION',
                'LISTO',
                'ENTREGADO',
                'ANULADO'
            )
        ),

    -- Cada estado habitual exige las fechas de sus etapas previas.
    -- ANULADO conserva las fechas alcanzadas antes de la anulación.
    CONSTRAINT ck_items_fechas_estado
        CHECK (
            (
                estado = 'PENDIENTE'
                AND fecha_preparacion IS NULL
                AND fecha_listo IS NULL
                AND fecha_entrega IS NULL
            )
            OR
            (
                estado = 'EN_PREPARACION'
                AND fecha_preparacion IS NOT NULL
                AND fecha_listo IS NULL
                AND fecha_entrega IS NULL
            )
            OR
            (
                estado = 'LISTO'
                AND fecha_preparacion IS NOT NULL
                AND fecha_listo IS NOT NULL
                AND fecha_entrega IS NULL
            )
            OR
            (
                estado = 'ENTREGADO'
                AND fecha_preparacion IS NOT NULL
                AND fecha_listo IS NOT NULL
                AND fecha_entrega IS NOT NULL
            )
            OR estado = 'ANULADO'
        ),

    CONSTRAINT ck_items_orden_fechas
        CHECK (
            (
                fecha_listo IS NULL
                OR (
                    fecha_preparacion IS NOT NULL
                    AND fecha_listo >= fecha_preparacion
                )
            )
            AND
            (
                fecha_entrega IS NULL
                OR (
                    fecha_listo IS NOT NULL
                    AND fecha_entrega >= fecha_listo
                )
            )
            AND
            (
                fecha_anulacion IS NULL
                OR (
                    (
                        fecha_preparacion IS NULL
                        OR fecha_anulacion >= fecha_preparacion
                    )
                    AND
                    (
                        fecha_listo IS NULL
                        OR fecha_anulacion >= fecha_listo
                    )
                    AND
                    (
                        fecha_entrega IS NULL
                        OR fecha_anulacion >= fecha_entrega
                    )
                )
            )
        ),

    CONSTRAINT ck_items_anulacion
        CHECK (
            (
                estado = 'ANULADO'
                AND fecha_anulacion IS NOT NULL
                AND anulado_por_id IS NOT NULL
            )
            OR
            (
                estado <> 'ANULADO'
                AND fecha_anulacion IS NULL
                AND anulado_por_id IS NULL
                AND motivo_anulacion IS NULL
                AND merma = FALSE
            )
        ),

    CONSTRAINT ck_items_motivo
        CHECK (
            motivo_anulacion IS NULL
            OR LENGTH(TRIM(motivo_anulacion)) > 0
        ),

    CONSTRAINT ck_items_merma
        CHECK (
            (
                estado = 'ANULADO'
                AND fecha_preparacion IS NOT NULL
                AND merma = TRUE
                AND motivo_anulacion IS NOT NULL
            )
            OR
            (
                (
                    estado <> 'ANULADO'
                    OR fecha_preparacion IS NULL
                )
                AND merma = FALSE
            )
        )
);

CREATE TABLE pagos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    registrado_por_id BIGINT NOT NULL,
    monto NUMERIC(12,2) NOT NULL,
    medio_pago VARCHAR(15) NOT NULL,
    fecha_pago TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pagos_cuenta
        FOREIGN KEY (cuenta_id)
        REFERENCES cuentas(id) ON DELETE RESTRICT,

    CONSTRAINT fk_pagos_usuario
        FOREIGN KEY (registrado_por_id)
        REFERENCES usuarios(id) ON DELETE RESTRICT,

    CONSTRAINT uq_pagos_cuenta UNIQUE (cuenta_id),
    CONSTRAINT ck_pagos_monto CHECK (monto >= 0),

    CONSTRAINT ck_pagos_medio
        CHECK (
            medio_pago IN (
                'EFECTIVO',
                'DEBITO',
                'CREDITO',
                'TRANSFERENCIA',
                'QR'
            )
        )
);

-- Solo una cuenta activa por mesa.
CREATE UNIQUE INDEX uq_cuentas_mesa_activa
    ON cuentas (mesa_id)
    WHERE estado IN ('ABIERTA', 'CUENTA_PEDIDA');

-- Índices para las consultas previstas.
CREATE INDEX idx_productos_categoria
    ON productos (categoria_id);

CREATE INDEX idx_cuentas_mesa_apertura
    ON cuentas (mesa_id, fecha_apertura);

CREATE INDEX idx_comandas_envio
    ON comandas (fecha_envio, id);

CREATE INDEX idx_items_comanda
    ON items_comanda (comanda_id);

CREATE INDEX idx_items_sector_estado
    ON items_comanda (destino, estado, comanda_id);

CREATE INDEX idx_pagos_fecha
    ON pagos (fecha_pago);

-- Las siguientes reglas se validarán transaccionalmente en el backend:
-- permisos por rol, transiciones de estado, comandas no vacías,
-- conservación de valores históricos, cuentas pagadas inmutables,
-- monto del pago igual al total y sincronización entre pago y cierre.
-- Este script no implementa esas operaciones.

COMMIT;