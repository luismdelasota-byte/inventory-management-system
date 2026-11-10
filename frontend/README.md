# Frontend — aplicación web

Este componente implementa la interfaz del sistema de gestión de inventario. Consume la API REST del backend y presenta las funciones disponibles según la sesión y el rol. La descripción de toda la solución está en el [README principal](../README.md).

## Responsabilidades

- Iniciar sesión y consultar el perfil del usuario.
- Mostrar el panel, sus métricas y alertas de stock.
- Consultar y administrar productos y categorías.
- Consultar existencias y estados de inventario.
- Registrar entradas y salidas y consultar sus movimientos.
- Mostrar la administración de usuarios a cuentas autorizadas.

Los datos se obtienen del backend; la interfaz no usa datos de demostración como sustituto de la API.

## Tecnologías

- React y TypeScript.
- Vite para desarrollo y compilación.
- React Router para navegación.
- Axios para comunicación HTTP.
- React Hook Form y Zod para formularios y validación.
- Tailwind CSS y Lucide React para estilos e iconografía.
- Vitest para pruebas.

## Organización del código

El código se encuentra en `src/`:

| Carpeta o archivo | Responsabilidad |
|---|---|
| `pages/` | Pantallas de login, panel, productos, categorías, inventario, movimientos, usuarios y perfil. |
| `components/` | Componentes reutilizables y diseño. |
| `context/` y `hooks/` | Estado compartido y hooks de interfaz. |
| `routes/` | Protección de rutas según sesión y rol. |
| `services/` | Cliente HTTP e integración con el backend. |
| `types.ts` | Tipos compartidos de TypeScript. |

## Requisitos

- Node.js 20.19 o posterior.
- npm.
- Backend disponible en la dirección configurada en `VITE_API_URL`.

## Configuración y ejecución

Desde PowerShell, en esta carpeta:

```powershell
npm install
Copy-Item .env.example .env
npm run dev
```

Vite muestra la dirección local en la terminal; normalmente será `http://localhost:5173`. El archivo `.env.example` contiene:

```env
VITE_API_URL=http://localhost:8080/api
```

Si la API está en otra dirección, cambia `VITE_API_URL` en `.env`. Las variables `VITE_*` pueden incluirse en el código que recibe el navegador; nunca pongas secretos o credenciales privadas en ellas.

## Scripts disponibles

| Comando | Descripción |
|---|---|
| `npm run dev` | Inicia el servidor de desarrollo de Vite. |
| `npm test` | Ejecuta las pruebas con Vitest. |
| `npm run lint` | Analiza el código con Oxlint. |
| `npm run build` | Comprueba TypeScript y compila para producción. |
| `npm run preview` | Previsualiza localmente la compilación. |

## Sesión y comunicación con la API

El cliente HTTP está centralizado en `src/services/api.ts`. Usa `VITE_API_URL` como dirección base, adjunta el token de sesión a las solicitudes protegidas y centraliza el tratamiento de respuestas y errores.

Las rutas de la interfaz requieren sesión; la administración de usuarios está restringida al rol administrador. Esta protección facilita la navegación, pero la autorización efectiva debe validarse en el backend.

## Compilación

```powershell
npm run build
```

Los archivos de producción se generan en `dist/`. Conserva `package-lock.json` para instalaciones reproducibles; no versionas `node_modules/`, archivos compilados ni `.env` locales.
