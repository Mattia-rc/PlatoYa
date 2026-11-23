-- PlatoYa — Datos de prueba para desarrollo (PYA-001).
-- Las columnas id son GENERATED ALWAYS: no se insertan ids.
-- Contraseña de todos los usuarios: 1234 (solo desarrollo), hasheada con BCrypt.

INSERT INTO usuarios (nombre, nombre_usuario, password_hash, rol) VALUES
    ('Ana Administración', 'admin',  '$2a$10$rp5deBsTYUD8jbfjRCDLgeo46l40L5yhkV.bg34ofpY8FaE0TJFTC', 'ADMIN'),
    ('Martín Mozo',        'mozo',   '$2a$10$aTjsgxnsxhbM6WmT3UZ1h.LGwN1W1YOTfR31zBEHODdADhn/VKrQe', 'MOZO'),
    ('Carla Cocina',       'cocina', '$2a$10$dFs25U8Ih.gErxg8Zvto2eSh1MrJv4Subi0OVxzYhW4UnmzqBzHZq', 'COCINA'),
    ('Bruno Barra',        'barra',  '$2a$10$z3YPgMrN8IpBiHyH3k857.VFE3fr8WvoVYVEpToE2WsvcIvginjOO', 'BARRA');

-- Mesas 1 a 8, todas LIBRE. La Mesa 7 es para 4 personas (caso §5).
INSERT INTO mesas (numero, capacidad) VALUES
    (1, 2),
    (2, 4),
    (3, 4),
    (4, 6),
    (5, 2),
    (6, 8),
    (7, 4),
    (8, 4);

INSERT INTO categorias (nombre, destino) VALUES
    ('Bebidas', 'BARRA'),
    ('Comidas', 'COCINA');

-- Productos del caso §5. Sin destino propio: lo heredan de su categoría (RN-13).
INSERT INTO productos (categoria_id, nombre, precio) VALUES
    ((SELECT id FROM categorias WHERE nombre = 'Bebidas'), 'Cerveza (pinta)',      6500.00),
    ((SELECT id FROM categorias WHERE nombre = 'Bebidas'), 'Gaseosa',              3500.00),
    ((SELECT id FROM categorias WHERE nombre = 'Comidas'), 'Hamburguesa completa', 14000.00),
    ((SELECT id FROM categorias WHERE nombre = 'Comidas'), 'Porción de papas',     7000.00);
