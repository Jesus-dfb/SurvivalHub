# Survival Hub

Survival Hub es una aplicacion web para gestionar mundos o servidores de juegos survival con amigos.

El proyecto esta pensado como portfolio junior: combina backend REST con Spring Boot, persistencia en PostgreSQL, autenticacion con JWT y un frontend sencillo servido desde la propia aplicacion.

## Funcionalidades

- Registro e inicio de sesion.
- Mundos privados por usuario.
- Miembros por mundo.
- Tareas por mundo.
- Recursos por tarea con cantidad necesaria y cantidad conseguida.
- Progreso calculado automaticamente.
- Boton directo para completar o reabrir tareas.
- Orden manual de tareas con arrastrar y soltar.
- Dashboard del mundo con resumen de progreso.
- Biblioteca de guias compartidas.
- Creacion de guias con pasos, recursos y video opcional.
- Favoritos y valoraciones de guias.
- Importacion de guias a un mundo como tareas con recursos.
- Bloqueo de importaciones duplicadas.
- Perfil del usuario con resumen de mundos, guias publicadas y guias guardadas.

## Stack

- Java 17
- Spring Boot
- Maven Wrapper
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- BCrypt
- HTML, CSS y JavaScript

Lombok esta disponible como dependencia, pero el codigo principal mantiene getters, setters y constructores explicitos para facilitar el aprendizaje.

## Arrancar en local

### 1. Crear base de datos

Crea una base de datos PostgreSQL llamada:

```text
survival_hub
```

### 2. Configurar credenciales

La aplicacion lee variables de entorno o un archivo local opcional.

Puedes crear en la raiz del proyecto:

```text
application-local.properties
```

Con este formato:

```properties
DB_URL=jdbc:postgresql://localhost:5432/survival_hub
DB_USERNAME=postgres
DB_PASSWORD=tu_password_de_postgres
JWT_SECRET=cambia-esta-clave-local-por-una-frase-larga
```

El archivo `application-local.properties` no debe subirse a Git. Usa `application-local.example.properties` como plantilla.

Tambien puedes usar variables de entorno:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/survival_hub"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_password_de_postgres"
$env:JWT_SECRET="una-clave-local-larga"
```

### 3. Ejecutar

Desde la raiz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir en el navegador:

```text
http://localhost:8080/
```

Endpoint de salud:

```text
GET http://localhost:8080/api/health
```

## Usuarios de demo

Al arrancar por primera vez se crean usuarios de ejemplo:

```text
alexcraft / password123
lunasurvival / password123
survivalfan / password123
```

Las contrasenas se guardan cifradas con BCrypt.

## Estructura principal

```text
src/main/java/com/survivalhub
|-- config
|-- controller
|-- model
|-- repository
|-- service
`-- SurvivalHubApplication.java
```

El frontend esta en:

```text
src/main/resources/static
|-- index.html
|-- app.js
`-- styles.css
```

## Endpoints principales

### Autenticacion

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

El login devuelve un token JWT. El frontend lo guarda en `localStorage` y lo envia en cada peticion protegida:

```text
Authorization: Bearer <token>
```

### Mundos

```text
GET    /api/worlds
GET    /api/worlds/{id}
POST   /api/worlds
PUT    /api/worlds/{id}
DELETE /api/worlds/{id}
```

Cada usuario ve solo sus propios mundos.

### Miembros

```text
GET    /api/worlds/{worldId}/members
GET    /api/worlds/{worldId}/members/{memberId}
POST   /api/worlds/{worldId}/members
PUT    /api/worlds/{worldId}/members/{memberId}
DELETE /api/worlds/{worldId}/members/{memberId}
```

### Tareas

```text
GET    /api/worlds/{worldId}/tasks
GET    /api/worlds/{worldId}/tasks/{taskId}
GET    /api/worlds/{worldId}/tasks/{taskId}/summary
POST   /api/worlds/{worldId}/tasks
PUT    /api/worlds/{worldId}/tasks/{taskId}
PUT    /api/worlds/{worldId}/tasks/order
DELETE /api/worlds/{worldId}/tasks/{taskId}
```

Las tareas incluyen:

- titulo;
- descripcion;
- prioridad;
- estado completado;
- orden manual dentro del mundo.

### Recursos

```text
GET    /api/worlds/{worldId}/tasks/{taskId}/resources
GET    /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
POST   /api/worlds/{worldId}/tasks/{taskId}/resources
PUT    /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
DELETE /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
```

Un recurso se considera completado cuando:

```java
collectedQuantity >= requiredQuantity
```

Ese estado se calcula automaticamente en el modelo `TaskResource`.

### Dashboard

```text
GET /api/worlds/{worldId}/dashboard
```

Devuelve resumen del mundo:

- miembros;
- tareas;
- tareas completadas;
- tareas pendientes;
- recursos;
- recursos completados;
- progreso medio.

### Biblioteca de guias

```text
GET    /api/guides
GET    /api/guides/{id}
POST   /api/guides
PUT    /api/guides/{id}
DELETE /api/guides/{id}
POST   /api/guides/{id}/rating
POST   /api/guides/{id}/favorite
DELETE /api/guides/{id}/favorite
POST   /api/guides/{guideId}/apply/worlds/{worldId}
```

Una guia puede incluir:

- titulo;
- juego;
- tipo;
- autor;
- dificultad;
- descripcion;
- video opcional;
- pasos;
- recursos necesarios;
- valoracion media;
- numero de votos;
- numero de importaciones.

Al aplicar una guia a un mundo, se crea una tarea nueva y se copian sus recursos con cantidad conseguida en `0`.

Si el mundo ya tiene una tarea con el mismo titulo que la guia, la API responde con `409 Conflict` para evitar duplicados.

### Usuarios

```text
GET /api/users
```

## Vistas del frontend

- **Mundos**: gestion de mundos, miembros, tareas y recursos.
- **Biblioteca**: guias compartidas, favoritos, votos e importacion a mundos.
- **Perfil**: resumen del usuario, mundos, guias publicadas y guardadas.

## Notas de base de datos

La configuracion base esta en:

```text
src/main/resources/application.properties
```

Configuracion relevante:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/survival_hub}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
app.jwt.secret=${JWT_SECRET:survival-hub-local-dev-secret-change-me}
server.port=${PORT:8080}
```

`spring.jpa.hibernate.ddl-auto=update` es practico durante desarrollo. Para una version mas seria se podria sustituir por migraciones con Flyway o Liquibase.

## Tests

El proyecto incluye una primera bateria sencilla de tests automatizados.

Para ejecutarlos desde la raiz del proyecto:

```powershell
.\mvnw.cmd test
```

Los tests usan una base de datos H2 en memoria con el perfil `test`, asi que no modifican la base de datos PostgreSQL local.

Actualmente se comprueba:

- endpoint de salud;
- registro e inicio de sesion;
- privacidad de mundos por usuario;
- bloqueo de importaciones duplicadas de guias;
- calculo automatico de recurso completado.

## Posibles mejoras futuras

- Recuperacion de contrasena.
- Edicion del perfil.
- Comentarios en guias.
- Moderacion de guias publicas.
- Ampliar cobertura de tests para miembros, tareas, recursos y biblioteca.
- Migraciones de base de datos.
- Frontend separado en React.
- Docker para levantar PostgreSQL y la app.
