# Backend — PlatoYa

## Estado

Carpeta destinada a la API del sistema.
En la segunda entrega contiene únicamente documentación de diseño.
No se incluyen controladores, servicios ni lógica de negocio implementada.

## Tecnologías seleccionadas

- Java y Spring Boot.
- Spring Security y JWT.
- Spring Data JPA, como propuesta de acceso a datos.
- PostgreSQL.
- Flyway para futuras migraciones.

## Organización prevista

El backend será un monolito organizado por módulos funcionales,
con separación de responsabilidades en:

- Controladores: solicitudes y respuestas HTTP.
- DTO: datos de entrada y salida.
- Servicios: reglas de negocio y transacciones.
- Entidades: representación de los datos persistentes.
- Repositorios: acceso a la base de datos.
- Seguridad: autenticación y autorización.
- Manejo de errores.

## Responsabilidades principales

- Verificar permisos por rol.
- Gestionar mesas, cuentas, comandas e ítems.
- Validar cambios de estado y anulaciones.
- Conservar los valores históricos de los consumos.
- Calcular el total y registrar el pago.
- Coordinar transacciones y operaciones simultáneas.

Estas responsabilidades se implementarán después de la aprobación
de la etapa de análisis y diseño.

## Documentación relacionada

- [Arquitectura](../docs/arquitectura.md)
- [Modelo de datos](../docs/modelo-datos.md)
- [Módulos funcionales](../docs/modulos.md)