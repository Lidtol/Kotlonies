<div align="center">

# 🐾 Kotlonies 🐾

### ฅ^•ﻌ•^ฅ　Gestión de colonias felinas　ฅ^•ﻌ•^ฅ

`(=^･ω･^=)　(=^･ｪ･^=)　(=①ω①=)　(=ↀωↀ=)　(=^‥^=)`

</div>

**Kotlonies** es una aplicación web para gestionar **colonias felinas**: registra las colonias y los gatos que viven en ellas, apunta incidencias, organiza las tareas de cuidado en un calendario y controla quién tiene acceso a cada cosa.

Está pensada para asociaciones y grupos de voluntariado que cuidan colonias de gatos callejeros y necesitan tener toda la información en un solo sitio.

> ᓚᘏᗢ　Proyecto de curso de 2º de DAW (Desarrollo de Aplicaciones Web).

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## ✨ Funcionalidades　(=^･ω･^=)

| Sección | Qué permite hacer |
|---|---|
| **Inicio** ฅ(^•ω•^ฅ) | Panel de resumen con estadísticas, colonias y actividad reciente, y accesos rápidos para crear gatos, colonias, incidencias o usuarios. |
| **Colonias** (=^‥^=)ﾉ | Crear, editar y archivar colonias (nombre, descripción, ubicación). El nombre de una colonia es único, también entre las archivadas. |
| **Gatos** (=ↀωↀ=) | Ficha de cada gato: nombre, sexo, fecha de nacimiento, color, estado, esterilización, identificación, colonia y observaciones. Con filtros. |
| **Incidencias** (=ＴェＴ=) | Registro de incidencias por tipo (salud, alimentación, abandono, seguridad, otro), prioridad (baja → urgente) y estado (abierta → cerrada), vinculadas a un gato o a una colonia. |
| **Calendario** (=｀ω´=) | Vista mensual de tareas (alimentación, visita, veterinario, revisión…) que se pueden crear por día, editar, completar y eliminar. |
| **Voluntariado** (^・ω・^ ) | *(Solo administradores)* Alta, edición, activación/desactivación y baja de las personas con acceso. |
| **Perfil** (=^ ◡ ^=) | Datos de la persona que ha iniciado sesión y cierre de sesión. |

### 👥 Roles　ฅ(=･ω･=)ฅ

| Rol | Consultar | Crear y editar | Archivar colonias / borrar gatos | Gestionar usuarios |
|---|:---:|:---:|:---:|:---:|
| `ADMIN` 👑 | ✅ | ✅ | ✅ | ✅ |
| `CUIDADOR` 🐟 | ✅ | ✅ | ❌ | ❌ |
| `CONSULTA` 👀 | ✅ | ❌ | ❌ | ❌ |

Si una persona se desactiva o se elimina mientras tiene la sesión abierta, su sesión se invalida en la siguiente petición. `(=ﾟωﾟ=)ﾉ ¡adiós!`

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 🛠️ Tecnologías　(=^･ｪ･^=)

**Backend** — `backend/kotlonies-api`
- Java 21 · Spring Boot 4.1.2 · Maven (con Maven Wrapper)
- Spring Web MVC, Spring Data JPA (Hibernate), Bean Validation
- Spring Security: sesión con cookie, contraseñas con BCrypt, protección CSRF y permisos por rol con `@PreAuthorize`
- MySQL (en ejecución) y H2 en memoria (en los tests)
- `springboot4-dotenv` para leer las credenciales desde un archivo `.env`

**Frontend** — `frontend`
- Angular 22 · TypeScript · RxJS
- SCSS/CSS propio, sin librerías de componentes (ni Material ni Bootstrap)
- Interceptor HTTP para enviar el token CSRF

**Calidad**
- Tests unitarios y de integración con JUnit y Spring Boot Test (repositorios, servicios, controladores y seguridad)
- Integración continua con GitHub Actions: tests del backend y build del frontend en cada push y pull request a `main`

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 📁 Estructura del proyecto　ᓚᘏᗢ

```
Kotlonies/
├── backend/
│   └── kotlonies-api/          # API REST con Spring Boot
│       ├── src/main/java/com/kotlonies_api/
│       │   ├── config/         # Seguridad, CORS, filtro de usuario activo, admin inicial
│       │   ├── controller/     # Endpoints REST
│       │   ├── dto/            # Objetos de petición y respuesta
│       │   ├── exception/      # Excepciones y manejador global de errores
│       │   ├── model/          # Entidades JPA y enums
│       │   ├── repository/     # Repositorios de Spring Data
│       │   └── service/        # Lógica de negocio
│       ├── src/test/           # Tests
│       └── .env.example        # Plantilla de variables de entorno
├── frontend/                   # Aplicación Angular
│   └── src/app/
│       ├── core/               # Servicios HTTP e interceptor CSRF
│       └── features/login/     # Pantalla de inicio de sesión
├── ejecucion.bat               # Arranca backend y frontend en Windows
├── ejecucion.sh                # Arranca backend y frontend en Linux
└── .github/workflows/ci.yml    # Integración continua
```

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 🚀 Puesta en marcha　~(=^‥^)ノ

### Requisitos

- **Java 21** o superior
- **Node.js 22** o superior (con npm)
- **MySQL 8** en marcha en `localhost:3306`

No hace falta instalar Maven: el proyecto incluye el wrapper (`mvnw` / `mvnw.cmd`). La base de datos `Kotlonies` se crea automáticamente la primera vez gracias a `createDatabaseIfNotExist=true`, y las tablas las genera Hibernate.

### 🐾 1. Clonar el repositorio

```bash
git clone https://github.com/Lidtol/Kotlonies.git
cd Kotlonies
```

### 🐾 2. Configurar las variables de entorno

Copia la plantilla y rellénala:

```bash
cd backend/kotlonies-api
cp .env.example .env
```

```env
# Conexión a MySQL
DB_USERNAME=root
DB_PASSWORD=tu_contraseña

# Administrador inicial (solo se crea si la base de datos no tiene usuarios)
APP_BOOTSTRAP_ADMIN_EMAIL=admin@kotlonies.local
APP_BOOTSTRAP_ADMIN_PASSWORD=una_contraseña_de_12_o_mas
```

- El archivo `.env` está en el `.gitignore`, así que tus credenciales no se suben al repositorio.
- La contraseña del administrador inicial debe tener **al menos 12 caracteres**; si no, la aplicación no arranca.
- Si no defines las variables del administrador, el backend arranca igualmente pero no crea ninguna cuenta (y aparece un aviso en el log).

### 🐾 3. Arrancar la aplicación

**Opción rápida** — desde la raíz del proyecto:

```bash
# Windows (instala las dependencias de Angular si faltan)
ejecucion.bat

# Linux
./ejecucion.sh
```

Se abren dos terminales, una con el backend y otra con el frontend. `ฅ(^•ﻌ•^ฅ)`

**Opción manual** — en dos terminales:

```bash
# Terminal 1: backend
cd backend/kotlonies-api
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
```

```bash
# Terminal 2: frontend
cd frontend
npm install
npm start
```

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API | http://localhost:8080/api |

Entra en el frontend con el email y la contraseña del administrador inicial y, desde **Voluntariado**, da de alta al resto de personas. ¡A cuidar gatitos! `(=^･ω･^=)♡`

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 🔌 API REST　(=①ω①=)

Todos los endpoints, salvo el login y el logout, requieren haber iniciado sesión. Las peticiones que modifican datos (`POST`, `PUT`, `PATCH`, `DELETE`) deben incluir la cabecera `X-XSRF-TOKEN` con el valor de la cookie `XSRF-TOKEN` (el frontend lo hace solo con su interceptor).

### Autenticación — `/api/auth`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/login` | Inicia sesión con email y contraseña |
| `GET` | `/sesion` | Devuelve la persona con la sesión activa |
| `POST` | `/logout` | Cierra la sesión |

### Recursos

| Recurso | Ruta base | Consultar | Crear / editar | Otras acciones |
|---|---|---|---|---|
| Colonias | `/api/colonies` | `GET /`, `GET /{id}` | `POST /`, `PUT /{id}` | `PATCH /{id}/archive` *(ADMIN)* |
| Gatos | `/api/cats` | `GET /`, `GET /{id}` | `POST /`, `PUT /{id}` | `DELETE /{id}` *(ADMIN)* |
| Incidencias | `/api/incidencias` | `GET /`, `GET /{id}` | `POST /`, `PUT /{id}` | `DELETE /{id}` |
| Tareas | `/api/tareas` | `GET /`, `GET /{id}` | `POST /`, `PUT /{id}` | `PATCH /{id}/completar`, `DELETE /{id}` |
| Usuarios | `/api/usuarios` | `GET /`, `GET /{id}` | `POST /`, `PUT /{id}` | `DELETE /{id}` |

Consultar está permitido a los tres roles; crear, editar y borrar, a `ADMIN` y `CUIDADOR`, salvo donde se indica. Todo `/api/usuarios` es solo para `ADMIN`.

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 🧪 Tests　(=･ｪ･=?

Los tests del backend usan una base de datos **H2 en memoria** en modo MySQL, así que no necesitan tener MySQL arrancado:

```bash
cd backend/kotlonies-api
./mvnw test
```

Para comprobar que el frontend compila:

```bash
cd frontend
npm run build
```

Lo mismo se ejecuta automáticamente en GitHub Actions en cada push y pull request a `main`.

---

<p align="center">🐾 ･ ･ ･ 🐾 ･ ･ ･ 🐾</p>

## 👩‍💻 Autora　ฅ^•ﻌ•^ฅ

**Lidtol** — estudiante de Desarrollo de Aplicaciones Web

[GitHub @Lidtol](https://github.com/Lidtol)

---

<div align="center">

Hecho con 💜 para todos los gatitos de colonia

`( =ω=)..zzZZ`

</div>
