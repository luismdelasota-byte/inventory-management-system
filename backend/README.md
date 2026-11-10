# Backend — API de inventario

Este componente implementa la API REST y las reglas del negocio del sistema. Para la visión del producto completo y la puesta en marcha conjunta con el frontend, consulta el [README principal](../README.md).

## Responsabilidades

- Autenticar usuarios y emitir tokens JWT.
- Aplicar permisos según los roles.
- Administrar productos, categorías y usuarios.
- Consultar existencias y alertas de stock.
- Registrar entradas y salidas y consultar el historial de movimientos.
- Proporcionar el resumen de información del panel.
- Validar solicitudes y persistir datos en PostgreSQL.

## Tecnologías

- Java 21 y Spring Boot 3.4.
- Spring Web, Spring Security y Spring Data JPA.
- PostgreSQL e Hibernate.
- Flyway para versionar y ejecutar migraciones.
- JWT para autenticación y BCrypt para codificar contraseñas.
- OpenAPI y Swagger para documentar la API.

## Organización

El código principal está en `src/main/java/com/inventory`:

| Paquete | Responsabilidad |
|---|---|
| `config/` | Configuración de Spring, CORS y seguridad. |
| `common/` | Respuestas, excepciones y componentes compartidos. |
| `security/` | Filtro JWT y carga de usuarios. |
| `modules/auth/` | Login y perfil autenticado. |
| `modules/products/` | Productos. |
| `modules/categories/` | Categorías. |
| `modules/inventory/` | Existencias y alertas. |
| `modules/movements/` | Entradas, salidas e historial. |
| `modules/users/` | Usuarios y roles. |
| `modules/dashboard/` | Resumen del panel. |

Según el módulo, las responsabilidades se distribuyen en controladores, DTO, servicios, repositorios, entidades y mappers.

## API y seguridad

- `POST /api/auth/login` permite iniciar sesión.
- Las demás rutas de la aplicación requieren autenticación JWT.
- La administración de productos, categorías y usuarios requiere el rol `ADMIN`.
- Las contraseñas se codifican con BCrypt.
- El backend valida permisos para cada petición; la protección visual del frontend no sustituye esta autorización.

Con el servidor iniciado:

- URL base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Persistencia y migraciones

Las migraciones de Flyway están en `src/main/resources/db/migration`:

| Migración | Contenido |
|---|---|
| `V1__create_users_and_roles.sql` | Usuarios, roles y roles iniciales `ADMIN` e `INVENTORY_USER`. |
| `V2__create_categories_and_products.sql` | Categorías y productos. |
| `V3__create_stock_movements.sql` | Movimientos de inventario y líneas de producto. |

Flyway aplica las migraciones al iniciar. Hibernate usa `ddl-auto: validate` para validar el esquema, no para crear o modificar las tablas. Las migraciones no crean usuarios ni inventario de demostración.

## Configuración y ejecución local

Requiere JDK 21 y una base de datos PostgreSQL creada. Desde PowerShell, en esta carpeta:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/inventory_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "tu_contraseña_de_postgres"
$env:JWT_SECRET = (node -e "console.log(require('crypto').randomBytes(64).toString('base64'))")
$env:FRONTEND_URL = "http://localhost:5173"

.\mvnw.cmd spring-boot:run
```

`JWT_SECRET` debe ser Base64 y representar al menos 64 bytes. Para crear un administrador al iniciar, configura `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` (mínimo 12 caracteres) y `ADMIN_FULL_NAME`. Si estas variables no están definidas, no se crea una cuenta.

El perfil `dev` está activo de forma predeterminada. El perfil `prod` requiere `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; se activa con `SPRING_PROFILES_ACTIVE=prod`.

## Pruebas

Desde esta carpeta:

```powershell
.\mvnw.cmd test
```

H2 está disponible para pruebas; la aplicación normal utiliza PostgreSQL.
