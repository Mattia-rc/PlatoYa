# Módulos funcionales — PlatoYa

**Entrega:** Segunda entrega — Diseño y módulos  
**Estado:** Alcance propuesto, pendiente de aprobación del tutor.

## 1. Objetivo

Organizar los módulos funcionales de PlatoYa para un bar con cocina
y servicio de mesa con mozo.

El circuito prioritario es:

Mesa → Comanda → Cocina/Barra → Estado → Entrega → Cuenta → Cierre.

Los módulos describen funcionalidades previstas.
Su inclusión en este documento no significa que estén implementadas.

## 2. Prioridades

- **P1 — Esencial:** forma parte del MVP y permite completar el circuito principal.
- **P2 — Deseable:** se abordará únicamente si el MVP está completo y el tiempo disponible lo permite.

La prioridad no representa un orden estricto de programación:
algunos módulos dependen de otros.

## 3. Listado de módulos

| ID | Módulo | Descripción | Usuarios | Prioridad |
|---|---|---|---|---|
| M01 | Autenticación y autorización | Inicio de sesión y control de acceso según los roles ADMIN, MOZO, COCINA y BARRA. | Todos | P1 |
| M02 | Gestión de usuarios | Alta, consulta, modificación y baja lógica de usuarios; asignación de roles. | ADMIN | P1 |
| M03 | Administración de mesas | Alta, consulta, modificación y baja lógica de mesas, con número y capacidad. | ADMIN | P1 |
| M04 | Gestión del catálogo | Administración de categorías y productos, precios y destinos de preparación; bajas lógicas. | ADMIN | P1 |
| M05 | Atención de mesas y cuentas | Apertura de cuentas, consulta de ocupación, solicitud de cuenta y liberación manual de mesas. | MOZO y ADMIN, según permisos | P1 |
| M06 | Carga y envío de comandas | Selección de productos, cantidades y observaciones. Cada envío genera una nueva comanda dentro de la cuenta activa. | MOZO | P1 |
| M07 | Preparación en cocina y barra | Visualización de los ítems de cada sector, ordenados por envío, y actualización a EN_PREPARACION y LISTO. | COCINA y BARRA | P1 |
| M08 | Seguimiento y entrega | Consulta del estado de los ítems y registro de su entrega al cliente. Actualización periódica de la pantalla del mozo. | MOZO | P1 |
| M09 | Anulaciones y trazabilidad | Anulación de ítems según estado y permisos, conservando autor, fecha, motivo cuando corresponda y marca de merma. | MOZO y ADMIN | P1 |
| M10 | Cuenta y registro del pago | Consulta del consumo acumulado, cálculo del total histórico y registro de un pago completo con su medio de pago. | ADMIN; MOZO consulta consumos | P1 |
| M11 | Reportes básicos | Consulta de importes cobrados por día, productos vendidos, tiempos de preparación y merma. | ADMIN | P2 |
| M12 | Notificaciones en tiempo real | Actualización mediante WebSocket y avisos visuales o sonoros cuando un ítem está listo. | MOZO, COCINA y BARRA | P2 |
| M13 | Reservas | Registro y consulta de reservas de mesas. Requiere análisis y diseño adicional antes de desarrollarse. | Por definir con el tutor | P2 |

## 4. Alcance y reglas principales del MVP

### M01 y M02 — Acceso y usuarios

- Cada usuario tiene uno de los cuatro roles definidos.
- El backend verificará los permisos de las operaciones.
- Las contraseñas se almacenarán mediante hash.
- Los usuarios dados de baja no podrán iniciar sesión.
- La baja lógica conservará las referencias a operaciones históricas.

### M03 y M05 — Mesas y cuentas

- Cada mesa tendrá como máximo una cuenta activa:
  ABIERTA o CUENTA_PEDIDA.
- Abrir una cuenta ocupará la mesa.
- Cualquier mozo podrá operar cualquier mesa.
- Se conservará quién abrió la cuenta y quién envió cada comanda.
- Si se agregan productos con la cuenta pedida, volverá a ABIERTA.
- Una cuenta pagada no se reabrirá.
- Registrar el pago no liberará automáticamente la mesa.
- MOZO o ADMIN podrán liberarla si no tiene cuentas activas.
- Solo ADMIN podrá anular una cuenta, bajo las condiciones
  pendientes de validación indicadas en el modelo de datos.

### M04 — Catálogo

- Cada producto pertenecerá a una categoría.
- La categoría establecerá un destino por defecto: COCINA o BARRA.
- El producto podrá definir un destino propio.
- Los cambios de precio no alterarán consumos ya registrados.
- Productos o categorías dados de baja no se ofrecerán
  para nuevos consumos.

### M06 — Comandas

- Cada envío generará una comanda nueva.
- Una comanda enviada contendrá al menos un ítem.
- No se editarán cantidades, productos u observaciones después del envío.
- Una corrección requerirá anular el ítem y cargar otro, según permisos.
- Las observaciones serán opcionales y no modificarán el precio.
- El mismo producto podrá aparecer en líneas separadas
  con observaciones distintas.
- Cada ítem conservará nombre, precio unitario y destino históricos.

### M07 y M08 — Preparación y entrega

- Cocina verá únicamente los ítems destinados a COCINA.
- Barra verá únicamente los ítems destinados a BARRA.
- Los ítems se agruparán por comanda y se ordenarán por hora de envío.
- Cada línea tendrá su propio estado.
- El sector registrará EN_PREPARACION y LISTO.
- El mozo registrará ENTREGADO.
- El MVP utilizará consultas periódicas (polling) para actualizar pantallas.
- WebSocket y avisos sonoros quedarán como mejoras deseables.
- Según el diseño propuesto, una línea con varias unidades compartirá
  estado y no admitirá entregas parciales.

### M09 — Anulaciones

- Los ítems anulados permanecerán registrados y no se cobrarán.
- El mozo podrá anular un ítem PENDIENTE.
- El mozo podrá anular un ítem EN_PREPARACION con motivo obligatorio.
- Solo ADMIN podrá anular ítems LISTO o ENTREGADO.
- Las anulaciones posteriores al inicio de preparación registrarán merma.
- El sector visualizará los ítems anulados tachados.
- No se podrán modificar consumos de una cuenta pagada.
- Estas políticas deberán contrastarse con el relevamiento real.

### M10 — Cuenta y pago

- El total se calculará sumando cantidad por precio unitario histórico
  de los ítems no anulados de todas las comandas.
- Para pagar, todos los ítems deberán estar ENTREGADO o ANULADO.
- Se admitirá un único pago por cuenta, por su total.
- Se registrarán monto, fecha, medio de pago y usuario responsable.
- Los medios serán EFECTIVO, DEBITO, CREDITO, TRANSFERENCIA o QR.
- Registrar el medio no implica procesar el pago mediante servicios externos.
- El pago y el cierre de la cuenta deberán realizarse
  en una misma transacción.

## 5. Dependencias funcionales

| Módulos | Dependencias principales |
|---|---|
| Gestión de usuarios, mesas y catálogo | Autenticación y autorización. |
| Atención de mesas y cuentas | Usuarios y mesas habilitados. |
| Carga y envío de comandas | Cuenta activa y catálogo disponible. |
| Preparación en cocina y barra | Comandas enviadas con ítems y destinos definidos. |
| Seguimiento y entrega | Estados registrados por los sectores. |
| Anulaciones | Ítems registrados, estado actual y permisos del usuario. |
| Cuenta y pago | Consumos de la cuenta y estados finales de sus ítems. |
| Reportes | Datos confiables de consumos, pagos y fechas de operación. |

## 6. Fuera de alcance

- Integración con pasarelas de pago, tarjetas o terminales POS.
- Pagos parciales y división de cuentas.
- Propinas y descuentos.
- Facturación fiscal y arqueo de caja.
- Control de stock e insumos.
- Impresión térmica y hardware específico.
- Modificadores de productos con costo adicional.
- Operación de delivery o pedidos en mostrador dentro del MVP.

## 7. Criterio de recorte

Si el tiempo disponible obliga a reducir el alcance, se pospondrán
los módulos P2 antes de recortar el circuito principal.

Las reservas requerirán ampliar el modelo de datos.
Los módulos deseables no se consideran comprometidos dentro del MVP.

## 8. Documentación relacionada

- [Presentación del proyecto](../README.md)
- [Diseño de base de datos](modelo-datos.md)
- [Esquema SQL](../database/esquema.sql)