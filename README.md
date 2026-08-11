# YouTube Playlist Checker 🎵

Proyecto final desarrollado para la Tecnicatura Universitaria en Programación. 

**YouTube Playlist Checker** es una herramienta web que permite a los usuarios monitorear el estado de sus listas de reproducción. Utilizando la **YouTube Data API v3**, el sistema analiza los videos de cualquier playlist, detecta aquellos que han sido eliminados, privatizados o restringidos, y almacena los resultados en una base de datos relacional. Esto permite generar estadísticas de disponibilidad y comparar el historial de una misma lista a lo largo del tiempo.

## 🚀 Características Principales

* **Validación de URLs:** Procesa enlaces de YouTube y extrae el ID de la playlist.
* **Análisis de Estado:** Consulta la API de YouTube para verificar la disponibilidad de cada video.
* **Detección de Motivos:** Clasifica por qué un video no está disponible (Privado, Eliminado, etc.).
* **Historial y Estadísticas:** Guarda los resultados en una base de datos para comparar análisis anteriores y calcular el porcentaje de disponibilidad.
* **Panel de Control:** Interfaz web intuitiva con grillas de resultados y métricas claras.

## 🛠️ Tecnologías Utilizadas

* **Backend:** Java 17+, Spring Boot
* **Base de Datos:** MySQL / PostgreSQL (Base de datos relacional)
* **Frontend:** HTML5, CSS3, Vanilla JavaScript
* **API Externa:** YouTube Data API v3
