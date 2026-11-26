# Backend — PlatoYa

API REST de PlatoYa. Es un monolito por capas con Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL 16 y Flyway.

## Requisitos

- Docker Desktop corriendo (para el entorno completo y para los tests).
- Para correrlo fuera de Docker: JDK 21 y Maven 3.9.

## Ejecutar con Docker (recomendado)

Desde la raíz del repositorio:

```bash
cp .env.example .env        # en Windows: copy .env.example .env
docker compose up --build
```

| Servicio | URL |
|---|---|
| Frontend (nginx) | http://localhost:4200 |
| Backend | http://localhost:8080/api |
| PostgreSQL | localhost:5432 |

- nginx redirige `/api` del front al backend.
- Si algún puerto está ocupado, se cambia en `.env`.
- Para bajar el entorno: `docker compose down`. Para borrar también los datos: `docker compose down -v`.

## Ejecutar desde Maven o el IDE

Con la base del compose levantada (`docker compose up db`):

```bash
mvn spring-boot:run
```

El datasource se toma de las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`. Por defecto apunta a `jdbc:postgresql://localhost:5432/platoya` con usuario y clave `platoya`. Hay que definirlas si el `.env` usa otros valores.

> **`JWT_SECRET` es obligatoria:** no tiene valor por defecto y el arranque falla si falta o tiene menos de 32 bytes. Al correr desde Maven o el IDE, definila como variable de entorno (podés copiar el valor de desarrollo de `.env.example`). Los tests ya tienen su propio secreto en el `pom.xml`.

> **Windows con zona horaria de Argentina:** la JVM informa la zona `America/Buenos_Aires`, que la imagen `postgres:16` no reconoce, y la conexión falla con `invalid value for parameter "TimeZone"`. Al correr desde el IDE, agregá la opción de VM `-Duser.timezone=America/Argentina/Buenos_Aires`. Los tests ya la tienen configurada en el `pom.xml`.

## Configuración

| Propiedad | Variable | Valor por defecto |
|---|---|---|
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/platoya` |
| `spring.datasource.username` | `DB_USER` | `platoya` |
| `spring.datasource.password` | `DB_PASSWORD` | `platoya` |
| `platoya.cors.allowed-origins` | `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |
| `platoya.jwt.secret` | `JWT_SECRET` | sin valor por defecto (32 bytes o más) |
| `platoya.jwt.expiracion` | — | `12h` |

- Todas las rutas van bajo `/api` (`server.servlet.context-path`).
- `spring.jpa.hibernate.ddl-auto=validate`: el esquema lo crea Flyway y JPA solo lo valida.

## Base de datos

Las migraciones están en `src/main/resources/db/migration`:

- `V1__esquema_inicial.sql`: copia de `database/esquema.sql` sin `BEGIN`/`COMMIT`.
- `V2__datos_prueba.sql`: datos de desarrollo:
  - usuarios `admin`, `mozo`, `cocina` y `barra`, todos con contraseña `1234` en BCrypt;
  - mesas 1 a 8 en estado `LIBRE`;
  - categorías Bebidas (BARRA) y Comidas (COCINA);
  - los productos del caso §5.

Una migración aplicada no se modifica. Los cambios de esquema van en una migración nueva (V3 o posterior).

## Errores de la API

Todos los errores responden con el formato que usa el front (`ApiError`):

```json
{ "mensaje": "texto legible", "codigo": "RN-01", "detalles": { "campo": "mensaje" } }
```

| Situación | HTTP | Campos |
|---|---|---|
| Body inválido (validación) | 400 | `mensaje`, `detalles` |
| Parámetro de ruta o de consulta inválido (tipo incorrecto o restricción como `@Min`) | 400 | `mensaje`, `detalles` por parámetro |
| Body con un valor de formato inválido (por ejemplo, un enum inexistente) | 400 | `mensaje`, `detalles` del campo |
| Body ilegible | 400 | `mensaje` |
| Recurso inexistente (`RecursoNoEncontradoException` o ruta inexistente) | 404 | `mensaje` |
| Regla de negocio (`NegocioException`) | 409 | `mensaje`, `codigo` |
| Error no controlado | 500 | `mensaje` genérico, sin stacktrace (el detalle va al log) |

## Tests

```bash
mvn test
```

Necesita Docker corriendo: `MigracionesTest`, `auth.AutenticacionTest` y `usuarios.UsuariosTest` levantan cada uno su PostgreSQL 16 con Testcontainers.

| Test | Qué cubre |
|---|---|
| `MigracionesTest` | Migraciones y datos de prueba contra PostgreSQL 16. |
| `auth.AutenticacionTest` | Login JWT sobre los usuarios de V2 (correcto, credenciales inválidas, usuario dado de baja, datos vacíos), vencimiento de 12 h, 401 por token ausente, vencido, de otra clave o mal formado, y 403 por rol. |
| `usuarios.UsuariosTest` | ABM de usuarios: alta con login posterior, validaciones (formato del usuario, recorte, campos obligatorios, rol inexistente), usuario repetido, listado, modificación con y sin contraseña, baja lógica y reactivación (RN-23), auto-baja y cambio del propio rol, 404, 403 por rol y 401 sin token. |
| `auth.JwtConfigTest` | `JWT_SECRET` ausente o de menos de 32 bytes. |
| `common.GlobalExceptionHandlerTest` | Formato y códigos de error (400, 404, 409, 500), incluido el 400 con el detalle del campo cuando el body trae un valor con formato inválido, como un enum inexistente. |
| `common.CorsConfigTest` | CORS: acepta el origen del front y rechaza otros. |

## Estructura

```text
src/main/java/com/platoya/
├── PlatoYaApplication.java
├── auth/        SecurityConfig, JwtConfig, AuthController, AuthService, LoginRequest, LoginResponse,
│                UsuarioSesion, CredencialesInvalidasException
├── usuarios/    Usuario, Rol, UsuarioRepository, UsuarioController, UsuarioService, UsuarioAltaRequest,
│                UsuarioModificacionRequest, UsuarioResponse
└── common/
    ├── error/   ErrorResponse, NegocioException, RecursoNoEncontradoException, GlobalExceptionHandler
    └── config/  CorsConfig
```

Cada módulo funcional (M01 a M10) agrega su paquete con su HU: controladores, DTO, servicios, entidades y repositorios. Los endpoints nuevos requieren token por defecto y cada módulo agrega su `@PreAuthorize` por rol.

## Documentación relacionada

- [Arquitectura](../docs/arquitectura.md)
- [Modelo de datos](../docs/modelo-datos.md)
- [Módulos funcionales](../docs/modulos.md)
