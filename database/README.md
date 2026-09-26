# Base de datos — PlatoYa

## Estado

Diseño relacional para PostgreSQL, pendiente de revisión y aprobación
del tutor. No representa una base desplegada.

## Archivos

- [esquema.sql](esquema.sql): definición de tablas, claves, restricciones
  e índices.
- [Modelo de datos y diagrama entidad-relación](../docs/modelo-datos.md):
  explicación del diseño, campos, tipos y relaciones.

## Entidades

1. usuarios
2. mesas
3. categorias
4. productos
5. cuentas
6. comandas
7. items_comanda
8. pagos

## Alcance del script

El archivo esquema.sql contiene DDL para crear la estructura
en una base de datos vacía.

No incluye datos reales, credenciales, lógica de aplicación
ni operaciones de cobro implementadas.

El script no está diseñado para ejecutarse repetidamente sobre
un esquema ya creado: no elimina ni reemplaza tablas existentes.

## Integridad

El diseño incluye:

- Claves primarias y foráneas.
- Restricciones de unicidad.
- Validaciones de estados, cantidades e importes.
- Un índice único parcial para impedir dos cuentas activas por mesa.
- Índices para las consultas principales.
- Bajas lógicas y conservación de referencias históricas.

Las reglas que involucran varias tablas, permisos o transiciones
se validarán transaccionalmente en el backend, como se detalla
en el modelo de datos.

## Evolución prevista

Después de la aprobación, el esquema inicial se incorporará
a las migraciones Flyway del backend.

Los cambios posteriores se registrarán en nuevas migraciones
versionadas.

## Validación

La ejecución técnica del script y la aprobación del diseño
deben registrarse cuando se realicen.
No se consideran completadas por la sola inclusión de estos archivos.