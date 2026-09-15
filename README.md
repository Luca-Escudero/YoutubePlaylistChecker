# YouTube Playlist Checker - Verificador de Playlists de YouTube

## Estructura del Repositorio
Este es un proyecto full-stack estructurado en dos carpetas principales:
* **`/youtube-playlist-checker`**: Backend desarrollado en Java 21 con Spring Boot, Spring Security y JWT.
* **`/playlist-front`**: Frontend desarrollado en Vanilla HTML, CSS (Bootstrap 5 + plantilla AdminLTE) y JS ES6.

## Stack Tecnológico
* **Backend:** Java 21, Spring Boot 3, Hibernate/JPA, MySQL, Spring Security + JWT.
* **Frontend:** HTML5, CSS3 (Bootstrap 5, AdminLTE), JavaScript Moderno (módulos ES6).

---

## ⚙️ Configuración y Puesta en Marcha

### 1. Configuración del Entorno (.env)
Para que el sistema se conecte a la base de datos local y firme los tokens JWT:
1. En la raíz del proyecto, duplicar `.env.example` y renombrarlo como `.env`.
2. Completar las credenciales:
   * `PLAYLIST_CHECKER_DB_NAME`: Nombre del esquema (ej: `playlist_checker_db`).
   * `PLAYLIST_CHECKER_DB_USER`: Usuario local de MySQL (ej: `root`).
   * `PLAYLIST_CHECKER_DB_PASSWORD`: Contraseña de MySQL.
   * `PLAYLIST_CHECKER_JWT_SECRET`: Clave secreta para JWT.
   * `YOUTUBE_API_KEY`: API Key de la YouTube Data API v3.

### 2. Base de Datos (Automática)
Al iniciar la aplicación, Hibernate creará automáticamente la base de datos y la estructura de tablas declaradas.

### 3. Ejecutar el Backend (Spring Boot)
Opción A: Ejecutar desde VS Code presionando **F5** (usa `.vscode/launch.json`).  
Opción B: Desde la terminal en la carpeta `/youtube-playlist-checker`:
```bash
./mvnw spring-boot:run
```

### 4. Ejecutar el Frontend
Sirva la carpeta `/playlist-front` usando un servidor estático local:

* **Opción recomendada (consola):** Ejecutar desde la raíz del proyecto:
  ```bash
  npx live-server playlist-front
  ```
  *(O alternativamente: `npx serve playlist-front`)*

* **Opción desde VS Code (Extensión Live Server):** Abrir **únicamente** la carpeta `/playlist-front` en VS Code y hacer clic en **Go Live**.

---

## 🐳 Opción Alternativa: Docker Compose

Si preferís levantar todo con Docker (MySQL + Backend + Frontend):
```bash
docker compose up --build
```
* **Frontend:** http://localhost:3000
* **Backend:** http://localhost:8080
