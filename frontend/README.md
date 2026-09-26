# Frontend — PlatoYa

## Estado

Carpeta destinada a la aplicación web.
En la segunda entrega contiene únicamente documentación de diseño.
La implementación comenzará después de la aprobación del tutor.

## Tecnologías seleccionadas

- Angular.
- TypeScript.

## Organización prevista

La aplicación se organizará por funcionalidades:

- Autenticación.
- Administración de usuarios.
- Administración de mesas.
- Categorías y productos.
- Atención de mesas y carga de comandas.
- Preparación en cocina y barra.
- Seguimiento y entrega de pedidos.
- Consulta de cuentas y registro del pago.

Los elementos comunes se organizarán en componentes reutilizables.
Los servicios de acceso a la API se separarán de la presentación.

## Comunicación

El frontend se comunicará con la API REST del backend mediante JSON.
No accederá directamente a PostgreSQL.

Las pantallas de trabajo utilizarán consultas periódicas para actualizar
los estados. Los permisos serán verificados por el backend.

## Documentación relacionada

- [Arquitectura](../docs/arquitectura.md)
- [Módulos funcionales](../docs/modulos.md)