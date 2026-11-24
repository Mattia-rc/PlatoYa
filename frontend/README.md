# Frontend — PlatoYa

Aplicación web de PlatoYa. Usa Angular 20, TypeScript y PrimeNG.

## Requisitos

- Para correrlo fuera de Docker: Node 24 y npm.
- Para probar contra la API real: el backend levantado (ver [README del backend](../backend/README.md)).

## Ejecutar con Docker

Desde la raíz del repositorio, `docker compose up --build` levanta todo el entorno.
El front queda en http://localhost:4200.

En Docker, el frontend se compila en modo producción y lo sirve nginx.
nginx redirige `/api` al backend (`nginx.conf`).

> **Los cambios de código no se ven automáticamente.** La imagen contiene el build
> generado al construirla. Para ver un cambio hay que reconstruir el servicio:
> `docker compose up --build -d frontend`.

## Ejecutar en modo desarrollo (recomendado para trabajar)

Desde `frontend/`:

```bash
npm install
npm start
```

La app queda en http://localhost:4200. Al guardar un archivo, el navegador se recarga solo.

Si el frontend de Docker también está corriendo, ambos usan el puerto 4200.
Hay que bajar el de Docker (`docker compose stop frontend`) o usar otro puerto (`npm start -- --port 4300`).
En ese caso, el backend tiene que aceptar ese origen en `CORS_ALLOWED_ORIGINS`.

### Mocks o API real

El comportamiento depende del environment:

| Archivo | Se usa en | `apiUrl` | `useMocks` |
|---|---|---|---|
| `src/environments/environment.ts` | `npm start` (desarrollo) | `http://localhost:8080/api` | `true` |
| `src/environments/environment.prod.ts` | `npm run build` y Docker | `/api` | `false` |

Con `useMocks: true`, `ApiService` devuelve datos simulados en memoria y **no llama al backend**.
Esto sirve para trabajar en pantallas sin levantar el backend.

Para probar contra la API real en desarrollo:

1. Levantar la base y el backend: `docker compose up db backend`, o el backend desde Maven o el IDE.
2. Cambiar `useMocks` a `false` en `environment.ts`.
3. Ejecutar `npm start`.

Hay que revisar este valor antes de commitear.

### Usuarios de prueba

`admin`, `mozo`, `cocina` y `barra`, todos con contraseña `1234`.
Sirven tanto con mocks como contra la API, que los recibe de las migraciones de Flyway.

## Scripts

| Comando | Acción |
|---|---|
| `npm start` | Servidor de desarrollo con recarga automática. |
| `npm run build` | Build de producción en `dist/frontend/browser`. |
| `npm test` | Tests unitarios con Karma y Jasmine. Abren Chrome. |

## Estructura

```
src/app/
├── core/
│   ├── auth/       # sesión, guards por rol
│   ├── http/       # ApiService, modelos e interceptores
│   └── polling/    # consultas periódicas de estado
├── features/
│   ├── login/
│   ├── mozo/       # mesas y carga de comandas
│   └── sector/     # pantalla de cocina y barra
└── layout/         # marco común de las pantallas autenticadas
```

Las pantallas de trabajo se actualizan por polling cada `pollingIntervalMs` (5 segundos).
Los permisos también se verifican en el backend.

## Documentación relacionada

- [Arquitectura](../docs/arquitectura.md)
- [Módulos funcionales](../docs/modulos.md)
