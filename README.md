# 🍽️ PlatoYa

Sistema web de gestión de mesas y comandas digitales para establecimientos gastronómicos.

## 📋 Descripción

**PlatoYa** es una solución orientada a bares, restaurantes y cervecerías que permite digitalizar la comunicación entre el salón, la cocina o barra y la caja.

El sistema busca reemplazar las comandas en papel y los pedidos verbales por un flujo digital centralizado. De esta manera, cada pedido queda registrado y vinculado con una mesa, permitiendo consultar su estado y calcular automáticamente el importe total.

Este proyecto se desarrolla como parte del Trabajo Final Integrador de la carrera.

## 🎯 Problema identificado

En establecimientos gastronómicos de mediana concurrencia, especialmente durante las horas pico, el uso de comandas en papel puede provocar:

- Pérdida o duplicación de pedidos.
- Errores de lectura o interpretación.
- Demoras en la comunicación entre mozos y cocina.
- Cobros incorrectos.
- Falta de información sobre el estado de los pedidos.
- Dificultades para obtener reportes de ventas.

## 💡 Solución propuesta

PlatoYa centraliza la gestión de mesas, pedidos y cobros mediante una aplicación web.

La plataforma permitirá:

- Registrar y administrar las mesas del establecimiento.
- Administrar categorías, platos y bebidas.
- Abrir una mesa y cargar sus consumos.
- Enviar comandas digitales a cocina o barra.
- Actualizar el estado de cada pedido.
- Consultar los pedidos listos para entregar.
- Calcular automáticamente el total de una mesa.
- Registrar el pago y cerrar la mesa.
- Consultar métricas básicas de facturación y ventas.

## 👥 Usuarios del sistema

### Mozo

- Abre y consulta mesas.
- Registra productos en una comanda.
- Envía pedidos a cocina o barra.
- Consulta el estado de los pedidos.

### Cocina o barra

- Visualiza las comandas recibidas.
- Consulta el orden de llegada.
- Cambia el estado de los pedidos.
- Marca los pedidos como listos.

### Cajero o administrador

- Administra mesas, categorías y productos.
- Consulta los consumos de cada mesa.
- Registra pagos y cierra cuentas.
- Consulta reportes básicos de ventas.

## 🚀 Alcance del MVP

La primera versión funcional de PlatoYa incluirá los siguientes módulos:

1. Autenticación y gestión de roles.
2. Gestión de usuarios.
3. Gestión de mesas.
4. Gestión de categorías y productos.
5. Apertura y seguimiento de mesas.
6. Registro de comandas.
7. Pantalla de pedidos para cocina o barra.
8. Actualización del estado de los pedidos.
9. Cálculo del total y cierre de mesa.
10. Métricas básicas de ventas.

## ✨ Funcionalidades opcionales

Si el tiempo de desarrollo lo permite, se podrán incorporar:

- Notificaciones visuales o sonoras.
- Actualizaciones en tiempo real.
- Reservas de mesas.
- Reportes y gráficos adicionales.

## 🚫 Fuera del alcance

En esta primera versión no se incluirán:

- Integraciones con pasarelas de pago.
- Integraciones con terminales POS.
- Impresión mediante impresoras térmicas.
- Facturación electrónica.
- Gestión de materias primas.
- Control avanzado de inventario.

## 🛠️ Tecnologías

> El stack tecnológico será confirmado durante la primera etapa del proyecto.

| Capa | Tecnología |
|---|---|
| Frontend | React + TypeScript |
| Backend | Node.js + Express + TypeScript |
| Base de datos | PostgreSQL |
| ORM | Prisma |
| Autenticación | JWT |
| Comunicación en tiempo real | Socket.IO |
| Contenedores | Docker y Docker Compose |
| Frontend en la nube | Vercel |
| Backend en la nube | Render |
| Base de datos en la nube | PostgreSQL |
| Control de versiones | Git y GitHub |

## 📁 Estructura prevista

```text
PlatoYa/
├── frontend/          # Aplicación web
├── backend/           # API y lógica de negocio
├── database/          # Scripts, modelos y documentación de la BD
├── docs/              # Informes y documentación académica
├── README.md
├── .gitignore
└── docker-compose.yml
