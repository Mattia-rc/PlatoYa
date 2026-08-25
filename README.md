# 🍽️ PlatoYa

> **Sistema Web de Gestión de Mesas y Comandas Digitales para Establecimientos Gastronómicos**  
> **Carrera:** Tecnicatura Universitaria en Programación a Distancia — UTN  
> **Entrega:** 1.ª Entrega — Propuesta de Proyecto y Repositorio  
> **Grupo:** 162  
> **Integrantes:** Bagni, Mattia | Petrei, Maximiliano  
> **Tutor asignado:** Londero, Oscar  

---

## 📋 1. Identificación y Análisis de la Problemática

### 1.1 Contexto y Descripción del Problema
En el sector gastronómico de mediana concurrencia (bares, cervecerías y restaurantes), la atención durante las horas pico suele presentar serias ineficiencias operativas. Actualmente, el flujo de trabajo depende de anotaciones manuales en papel (comandas físicas) o mensajes verbales hacia la cocina/barra. Esto genera:

- Pérdida de comandas o errores de lectura/interpretación.
- Cobros erróneos en la caja al no coincidir lo realmente servido con lo registrado.
- Cuellos de botella en la comunicación entre los mozos en el salón y el personal de cocina/barra.

Digitalizar este proceso no consiste únicamente en reemplazar el papel por una pantalla, sino en **transformar el flujo de trabajo** para brindar visibilidad en tiempo real del estado de cada mesa y pedido.

---

### 1.2 Identificación de Actores
- **Mozo:** Encargado de tomar pedidos por mesa, enviarlos a cocina/barra y consultar el estado de elaboración para servirlos.
- **Cocina / Barra:** Recibe las comandas digitales ordenadas por orden de llegada, actualiza el estado (*Pendiente* $\rightarrow$ *En preparación* $\rightarrow$ *Listo*) y notifica al mozo.
- **Cajero / Administrador:** Controla la ocupación del salón, administra productos/mesas, cierra las cuentas de las mesas, procesa cobros y analiza métricas de ventas por plato y horario.

### 👥 Usuarios del sistema

#### Mozo

- Abre y consulta mesas.
- Registra productos en una comanda.
- Envía pedidos a cocina o barra.
- Consulta el estado de los pedidos.

#### Cocina o barra

- Visualiza las comandas recibidas.
- Consulta el orden de llegada.
- Cambia el estado de los pedidos.
- Marca los pedidos como listos.

#### Cajero o administrador

- Administra mesas, categorías y productos.
- Consulta los consumos de cada mesa.
- Registra pagos y cierra cuentas.
- Consulta reportes básicos de ventas.
---

### 1.3 Impacto Medible
- **Demoras operativas:** Demoras de hasta **20 a 30 minutos** en servir a las mesas ocupadas por mala priorización o extravío de comandas.
- **Rotación lenta:** Pérdida de rotación de clientes en horas pico debido a la tardanza en la entrega de la cuenta o la preparación de platos.
- **Pérdida económica directa:** Errores de facturación de entre un **5% y 8% mensual** por consumos no cobrados o comandas traspapeladas.
- **Deterioro de la reputación digital y pérdida de clientes:** Un alto porcentaje de reseñas negativas en plataformas digitales está directamente asociado a "demoras en la comida" o "pedidos erróneos". Se estima que **4 de cada 10 clientes potenciales** evitan visitar un local con una puntuación menor a 4 estrellas.

---

### 1.4 Propuesta de Valor Agregado
La solución aporta valor real al:
1. **Reducir los tiempos de espera** mediante una comandera digital centralizada que sincroniza el salón con la cocina al instante.
2. **Eliminar cobros erróneos** al vincular de forma automática cada consumo registrado con el total de la mesa.
3. **Proporcionar un panel de métricas** de facturación que permite identificar los platos más vendidos y las franjas horarias de mayor demanda para optimizar insumos y personal.
4. **Proteger la reputación del negocio** manteniendo un estándar alto de atención y disminuyendo el margen de error humano.

---

## 🛠️ 2. Definición y Justificación del Stack Tecnológico

Siguiendo el principio de seleccionar tecnologías maduras que el equipo ya domina o puede abordar con una curva de aprendizaje controlada:

| Capa | Tecnología Seleccionada | Justificación Técnica |
| :--- | :--- | :--- |
| **Frontend** | Angular | Ofrece un desarrollo de componentes reutilizables (mesas, comandas, menú) y una rápida actualización de la interfaz de usuario ante cambios de estado. |
| **Backend** | Java + Spring Boot | Excelente manejo de la lógica de negocio (cambios de estados de comandas, cálculo de totales) y arquitectura REST robusta con tipado seguro. |
| **Base de Datos** | PostgreSQL + Flyway | Sistema de base de datos relacional indispensable para mantener la integridad transaccional (ACID) entre mesas, pedidos, productos y facturación. Flyway permite mantener versionados los cambios de la estructura de la base de datos. |
| **Autenticación** | JWT (JSON Web Tokens) | Gestión segura de sesiones y control de acceso basado en roles (`Admin`, `Mozo`, `Cocinero`). |
| **Plataforma / Despliegue** | Docker + PaaS (Render / Railway) | Uso de contenedores Docker para garantizar la paridad entre el entorno de desarrollo y producción, desplegado en un PaaS a definir sin costo operativo de infraestructura. |

---

## 🚀 3. Alcance del Proyecto (Definición del MVP)

Para garantizar la viabilidad temporal del desarrollo en el marco de la cursada, se clasifica el alcance en tres niveles:

### 3.1 Mínimo Viable (MVP)
- **Gestión de Menú y Mesas (ABM/CRUD):** Alta, baja y modificación de categorías, platos/bebidas (con precio) y configuración de mesas.
- **Módulo Mozo (Toma de Comandas):** Asignación de mesa, selección de productos del menú, envío de comandas a cocina y seguimiento de estado.
- **Módulo Cocina/Barra (Comandera Digital):** Vista de pedidos entrantes ordenados por llegada con actualización de estados (*Pendiente* $\rightarrow$ *En preparación* $\rightarrow$ *Listo*).
- **Módulo Caja (Cierre de Mesa):** Visualización del consumo total acumulado por mesa y marcas de cierre/pago.
- **Métricas Básicas:** Reporte de facturación diaria y platos más vendidos.

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

### 3.2 Deseables (*Nice to have*)
- Notificaciones visuales o sonoras en tiempo real cuando un plato pasa a estado *Listo*.
- Módulo de reservas previas de mesas por parte de los clientes.

### 3.3 Fuera de Alcance (*Out of Scope*)
- Integración con pasarelas de pago externas o terminales POS físicos de tarjetas.
- Impresión térmica mediante hardware específico.
- Módulo de control de stock e insumos de materia prima (se acota únicamente a catálogo de productos finales).

---

## 📅 4. Plan de Trabajo e Hitos de Ejecución

El plan de trabajo se organiza en sprints/semanas priorizando la viabilidad técnica:

- **Semanas 1-2:** Configuración del repositorio, arquitectura base en Docker y diseño del esquema de Base de Datos (Entidades: `Usuarios`, `Mesas`, `Productos`, `Pedidos`, `DetallePedido`).
- **Semanas 3-4:** Desarrollo de las APIs REST para el CRUD de productos, mesas y gestión de autenticación/roles (`Admin`, `Mozo`, `Cocinero`).
- **Semanas 5-6:** Desarrollo del Frontend en Angular para la toma de comandas (Mozo) y la pantalla de cocina (Comandera).
- **Semanas 7-8:** Integración del Módulo Caja (Cierre de mesa y reportes), pruebas integrales (*End-to-End*) y despliegue en entorno de prueba.

---

## ⚖️ 5. Análisis de Viabilidad

- **Viabilidad Técnica:** El equipo cuenta con conocimientos sobre la arquitectura cliente-servidor elegida y bases de datos relacionales, evitando dependencias con herramientas externas complejas.
- **Viabilidad Operativa:** La solución responde directamente al flujo habitual de un restaurante (Mozo $\rightarrow$ Cocina $\rightarrow$ Caja), garantizando una adopción sencilla por parte de los usuarios sin necesidad de extensas capacitaciones.
- **Viabilidad Temporal:** El recorte explícito del alcance (MVP) permite entregar un producto 100% funcional y probado dentro de las semanas fijadas por el calendario académico.

---

## 📁 6. Estructura del Proyecto

Organización en monorepo para simplificar la evaluación académica y el control de versiones:

```text
PlatoYa/
├── frontend/          # Aplicación Web en Angular
├── backend/           # API REST en Java + Spring Boot
├── database/          # Migraciones Flyway y scripts de BD
├── docs/              # Informes y documentación académica
├── README.md          # Documento de presentación de la propuesta
├── .gitignore
└── docker-compose.yml # Orquestación local para desarrollo
