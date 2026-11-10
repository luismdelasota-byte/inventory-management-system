# Sistema de gestión de inventario

Aplicación web full-stack para administrar productos, categorías y existencias. Permite registrar entradas y salidas de inventario, consultar movimientos y alertas de stock, y administrar usuarios mediante roles.

## Estado del proyecto

Actualmente estoy trabajando en el despliegue de la aplicación para que pueda probarse en línea. Cuando esté disponible, agregaré aquí el enlace de acceso y las indicaciones necesarias para conocer sus funciones. Mientras tanto, es posible ejecutar el proyecto localmente siguiendo la guía de [Inicio local](#inicio-local).

## Funcionalidades

- Inicio de sesión y consulta del perfil autenticado.
- Panel con resumen, movimientos recientes y alertas de stock.
- Consulta, búsqueda y administración de productos y categorías.
- Consulta de existencias: disponible, stock bajo o sin existencias.
- Registro e historial de entradas y salidas de inventario.
- Administración de usuarios con permisos según el rol.

Las operaciones de administración de productos, categorías y usuarios requieren el rol `ADMIN`. El stock se modifica mediante movimientos, no desde la edición del producto.

## Tecnologías

| Componente | Tecnologías |
|---|---|
| Frontend | React, TypeScript, Vite, React Router, Axios |
| Backend | Java 21, Spring Boot 3.4, Spring Web |
| Seguridad | Spring Security, JWT y BCrypt |
| Base de datos | PostgreSQL, Spring Data JPA e Hibernate |
| Migraciones | Flyway |
| API | REST, OpenAPI y Swagger |
| Pruebas | Vitest; Spring Boot Test, JUnit y H2 |

El frontend consume datos reales de la API. La aplicación no agrega cuentas ni datos de inventario de demostración automáticamente.

## Estructura del repositorio

```text
.
├── backend/
│   └── README.md   # Documentación específica del backend
├── frontend/
│   └── README.md   # Documentación específica del frontend
└── README.md       # Guía general del proyecto
```

El backend separa sus dominios en módulos: autenticación, productos, categorías, inventario, movimientos, usuarios y panel. El frontend contiene páginas, componentes compartidos, rutas protegidas, estado de aplicación y servicios HTTP.

## Requisitos

- Node.js 20.19 o posterior y npm.
- JDK 21.
- PostgreSQL.
- PowerShell para ejecutar literalmente los comandos de esta guía (pueden adaptarse a otra terminal).

El backend incluye Maven Wrapper, así que no hace falta instalar Maven globalmente.

## Inicio local

### 1. Crear la base de datos

Inicia PostgreSQL y crea una base vacía, por ejemplo `inventory_db`. Flyway crea las tablas y roles al iniciar el backend.

### 2. Iniciar el backend

En PowerShell, desde la raíz del proyecto:

```powershell
Set-Location .\backend

$env:DB_URL = "jdbc:postgresql://localhost:5432/inventory_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "tu_contraseña_de_postgres"
$env:JWT_SECRET = (node -e "console.log(require('crypto').randomBytes(64).toString('base64'))")
$env:FRONTEND_URL = "http://localhost:5173"

.\mvnw.cmd spring-boot:run
```

La API estará disponible en `http://localhost:8080/api`. Mantén abierta esta terminal.

### 3. Iniciar el frontend

Abre otra terminal:

```powershell
Set-Location .\frontend
npm install
Copy-Item .env.example .env
npm run dev
```

Abre `http://localhost:5173` e inicia sesión con una cuenta que exista en la base de datos. Las migraciones crean los roles `ADMIN` e `INVENTORY_USER`, pero no crean usuarios ni productos de prueba.

#### Administrador inicial (opcional)

Antes de iniciar el backend, puedes definir las cuatro variables siguientes para que se cree un administrador al arrancar:

```powershell
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_EMAIL = "admin@example.com"
$env:ADMIN_PASSWORD = "una_contraseña_segura_de_al_menos_12_caracteres"
$env:ADMIN_FULL_NAME = "Administrador"
```

La contraseña debe tener al menos 12 caracteres. No uses credenciales de ejemplo en un entorno real. Si omites estas variables, el inicializador no crea una cuenta.

## Configuración

### Backend

| Variable | Descripción | Valor predeterminado |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/inventory_db` en desarrollo |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` en desarrollo |
| `DB_PASSWORD` | Contraseña de PostgreSQL | Vacío; debe configurarse |
| `JWT_SECRET` | Secreto Base64 para firmar tokens | Obligatorio |
| `JWT_EXPIRATION` | Vigencia del token, en milisegundos | `86400000` (24 horas) |
| `FRONTEND_URL` | Origen autorizado para CORS | `http://localhost:5173` |
| `ADMIN_USERNAME` | Usuario administrador inicial, opcional | Vacío |
| `ADMIN_EMAIL` | Correo del administrador inicial | Vacío |
| `ADMIN_PASSWORD` | Contraseña inicial (mínimo 12 caracteres) | Vacío |
| `ADMIN_FULL_NAME` | Nombre completo del administrador inicial | Vacío |

`JWT_SECRET` debe estar codificado en Base64 y representar al menos 64 bytes. Puedes generarlo localmente con Node.js:

```powershell
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

El perfil `dev` se activa de forma predeterminada. El perfil `prod` requiere `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; se activa con `SPRING_PROFILES_ACTIVE=prod`.

### Frontend

`frontend/.env.example` contiene:

```env
VITE_API_URL=http://localhost:8080/api
```

Cópialo como `frontend/.env` y cambia el valor si la API usa otra dirección. Las variables `VITE_*` se incluyen en el código enviado al navegador: no pongas allí secretos, contraseñas ni credenciales privadas.

## API

La URL base local es `http://localhost:8080/api`.

| Módulo | Endpoints principales |
|---|---|
| Autenticación | `POST /auth/login`, `GET /auth/me` |
| Panel | `GET /dashboard/summary` |
| Productos | `GET /products`, `GET /products/{id}`, `POST /products`, `PUT /products/{id}`, `PATCH /products/{id}/status` |
| Categorías | `GET /categories`, `GET /categories/active`, `GET /categories/{id}`, `POST /categories`, `PUT /categories/{id}`, `PATCH /categories/{id}/status` |
| Inventario | `GET /inventory`, `GET /inventory/low-stock` |
| Movimientos | `GET /movements`, `GET /movements/{id}`, `POST /movements/in`, `POST /movements/out` |
| Usuarios | `GET /users`, `GET /users/{id}`, `POST /users`, `PUT /users/{id}`, `PATCH /users/{id}/status` |

El login es público; las demás rutas de la aplicación requieren autenticación. Con el backend activo, la documentación interactiva está en [Swagger UI](http://localhost:8080/swagger-ui.html) y la especificación OpenAPI en `http://localhost:8080/api-docs`.

## Base de datos

Las migraciones Flyway se encuentran en `backend/src/main/resources/db/migration`:

1. `V1__create_users_and_roles.sql`: crea usuarios, roles y los roles iniciales `ADMIN` e `INVENTORY_USER`.
2. `V2__create_categories_and_products.sql`: crea categorías y productos.
3. `V3__create_stock_movements.sql`: crea movimientos y sus líneas de producto.

Flyway aplica las migraciones al iniciar. Hibernate usa `ddl-auto: validate`: valida el esquema, pero no crea ni modifica automáticamente las tablas. Antes de usar una base con tablas previas, verifica el historial de Flyway y la compatibilidad del esquema.

## Pruebas y compilación

Desde `backend`:

```powershell
.\mvnw.cmd test
```

Desde `frontend`:

```powershell
npm test
npm run lint
npm run build
```

La compilación del frontend comprueba TypeScript y genera los archivos de producción en `frontend/dist`.

## Documentación por componente

- [Backend](backend/README.md): API, seguridad, módulos, base de datos, configuración y pruebas del servidor.
- [Frontend](frontend/README.md): interfaz, estructura, configuración, scripts y pruebas del cliente.

## Seguridad y publicación

- No subas archivos `.env` con valores reales, contraseñas, tokens ni secretos JWT.
- No pongas secretos en variables `VITE_*`, ya que son accesibles desde el navegador.
- Configura los secretos de producción fuera del repositorio.
- Los `.gitignore` de `backend` y `frontend` excluyen archivos locales y artefactos generados. Conserva los manifiestos y lockfiles necesarios para reconstruir el proyecto.
