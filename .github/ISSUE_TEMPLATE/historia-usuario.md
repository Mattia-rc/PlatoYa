---
name: Historia de usuario
about: HU formal PYA-NNN con criterios de aceptación, escenarios Gherkin, DoR y DoD.
title: "PYA-NNN <título>"
---

## PYA-NNN — <título>

**Responsable:** <integrante> · **Período:** S<n> · **Módulos:** M0x · **Reglas:** RN-xx, RN-yy

### Historia
Como <rol>
quiero <acción>
para <valor>.

### Criterios de aceptación
1. ...
2. ...

### Escenarios (BDD)
```gherkin
# language: es
Característica: <nombre>

  Escenario: <camino feliz>
    Dado ...
    Cuando ...
    Entonces ...

  Escenario: <alternativo o borde>
    ...

  Escenario: <error o permiso>
    ...
```

### Dependencias
- Depende de: PYA-xxx
- Contrato de API (si cruza con el otro integrante): ...

### Datos de prueba
- ...

### Definition of Ready
- [ ] Tiene código `PYA-NNN`, responsable, milestone del período y labels.
- [ ] La narrativa indica rol, necesidad y valor.
- [ ] Referencia los módulos (M0x) y las reglas de negocio (RN-xx) que cubre.
- [ ] Tiene criterios de aceptación en prosa.
- [ ] Tiene escenarios Gherkin que cubren el camino feliz, al menos un caso alternativo o borde, y los casos de error o de permiso por rol que apliquen.
- [ ] Las dependencias están identificadas, y las HU de las que depende están mergeadas o tienen el contrato de API acordado.
- [ ] Si usa endpoints o pantallas del otro integrante, el contrato (endpoint, DTO, errores) está acordado en la issue.
- [ ] Los datos de prueba necesarios están identificados (V2 o nuevos).
- [ ] Entra en el período asignado. Si no entra, se divide en otra HU.

### Definition of Done
- [ ] El código está en `main` mediante un PR aprobado por el otro integrante.
- [ ] Cada escenario Gherkin tiene un test JUnit cuyo nombre lo referencia o un paso en la checklist manual, y pasa.
- [ ] `mvn test` está en verde y el build de Angular termina sin errores.
- [ ] Las reglas y los permisos se validan en el backend; ocultar botones en el front no alcanza.
- [ ] El esquema no se modificó, salvo con una migración Flyway nueva (V3 o posterior).
- [ ] La aplicación levanta con `docker-compose up` y el recorrido de la HU funciona de punta a punta.
- [ ] La documentación del repo se actualizó solo si cambió una regla, como la ampliación de RN-11.
- [ ] Hay entrada en la bitácora del responsable, si hubo dificultades.
- [ ] Hay entrada en el registro de uso de IA del responsable, con la sesión de plan de la HU resumida.
- [ ] La issue quedó cerrada por el PR (`Closes #<n.º>`).
