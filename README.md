# Survival Hub

Survival Hub es una aplicacion web sencilla para gestionar mundos o servidores survival con amigos.

Permite trabajar con:

- mundos privados por usuario;
- miembros;
- tareas;
- recursos necesarios para cada tarea;
- progreso de recursos calculado automaticamente;
- biblioteca online simulada para anadir guias a un mundo;
- login con token para publicar y votar guias.

La aplicacion ya usa PostgreSQL mediante Spring Data JPA. Los datos se guardan en base de datos local.

## Stack

- Java
- Spring Boot
- Maven Wrapper
- API REST
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Lombok disponible, aunque el codigo principal usa getters y setters explicitos
- HTML, CSS y JavaScript servidos desde Spring Boot

## Arrancar el proyecto

Antes de arrancar, crea una base de datos PostgreSQL llamada:

```text
survival_hub
```

Por defecto la aplicacion intenta conectar con:

```properties
DB_URL=jdbc:postgresql://localhost:5432/survival_hub
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

Si tu usuario o password son distintos, puedes cambiarlos en PowerShell antes de arrancar:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_password"
$env:DB_URL="jdbc:postgresql://localhost:5432/survival_hub"
```

Tambien puedes crear un archivo local en la raiz del proyecto llamado:

```text
application-local.properties
```

Con este contenido:

```properties
DB_URL=jdbc:postgresql://localhost:5432/survival_hub
DB_USERNAME=postgres
DB_PASSWORD=tu_password_real_de_postgres
JWT_SECRET=una-clave-local-larga-para-desarrollo
```

Este archivo esta ignorado por Git para no subir tu contrasena. Hay una plantilla en `application-local.example.properties`.

Desde la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

Abrir:

```text
http://localhost:8080/
```

La pantalla principal tiene dos apartados:

- `Mundos`: gestion de mundos, miembros, tareas y recursos.
- `Biblioteca`: zona simulada donde se publican guias reutilizables con un usuario activo.

Endpoint de salud:

```text
http://localhost:8080/api/health
```

## Endpoints principales

### Autenticacion

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

El registro y el login devuelven un token. El frontend lo guarda en el navegador y lo envia en cada peticion protegida con:

```text
Authorization: Bearer token
```

Usuarios de demo:

```text
alexcraft / password123
lunasurvival / password123
survivalfan / password123
```

Si esos usuarios ya existian antes sin contrasena, la aplicacion les asigna `password123` al arrancar.

### Mundos

```text
GET    /api/worlds
GET    /api/worlds/{id}
POST   /api/worlds
PUT    /api/worlds/{id}
DELETE /api/worlds/{id}
```

Cada usuario autenticado ve solo sus propios mundos. Al crear un mundo, la API lo asocia automaticamente al usuario del token. Los mundos antiguos sin propietario se asignan a `alexcraft` durante el arranque para conservar los datos de demo.

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
DELETE /api/worlds/{worldId}/tasks/{taskId}
```

Las tareas incluyen:

- titulo;
- descripcion;
- prioridad;
- fecha objetivo;
- estado completado.

### Recursos

```text
GET    /api/worlds/{worldId}/tasks/{taskId}/resources
GET    /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
POST   /api/worlds/{worldId}/tasks/{taskId}/resources
PUT    /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
DELETE /api/worlds/{worldId}/tasks/{taskId}/resources/{resourceId}
```

### Dashboard

```text
GET /api/worlds/{worldId}/dashboard
```

Devuelve un resumen del mundo:

- numero de miembros;
- numero de tareas;
- tareas completadas y pendientes;
- recursos totales;
- recursos completados;
- progreso medio de tareas.

### Usuarios

```text
GET /api/users
```

Los usuarios ya se guardan en base de datos y tienen contrasena cifrada con BCrypt. La pantalla usa el usuario autenticado, no un selector manual.

Ese usuario se usa para:

- publicar nuevas guias como autor;
- votar guias;
- actualizar un voto anterior si el mismo usuario vuelve a votar la misma guia;
- filtrar la biblioteca por `Todas las guias` o `Mis guias`.

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

Una guia puede representar una ruta de progreso, una estructura, una farm o una guia de boss. Incluye:

- titulo;
- juego;
- tipo;
- autor;
- dificultad;
- valoracion media;
- numero de votos;
- numero de importaciones;
- fecha de creacion;
- descripcion;
- enlace opcional a video;
- pasos;
- recursos necesarios.

Al anadir una guia a un mundo, la API crea automaticamente una tarea nueva y copia sus recursos con `collectedQuantity` en `0`.

Las valoraciones se guardan en la tabla `guide_ratings`. Cada voto incluye:

- guia votada;
- nombre del usuario activo;
- puntuacion de `1` a `5`;
- fecha de creacion.

Si el mismo usuario activo vota otra vez la misma guia, se actualiza su voto anterior. Despues se recalcula la media de la guia desde la tabla de votos.

La biblioteca es compartida entre usuarios: todos pueden ver las guias publicadas. Publicar, votar y anadir guias al mundo requiere login. Editar o borrar una guia queda limitado al autor. Mas adelante podra tener favoritos, comentarios o moderacion.

Cada usuario puede guardar guias como favoritas y filtrarlas desde la biblioteca con `Guardadas`.

## Regla de progreso

Un recurso se considera completado cuando:

```java
collectedQuantity >= requiredQuantity
```

Ese estado no se guarda a mano. Se calcula en `TaskResource`.

## Nota sobre base de datos

La configuracion actual esta en `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/survival_hub}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update
app.jwt.secret=${JWT_SECRET:survival-hub-local-dev-secret-change-me}
```

`spring.jpa.hibernate.ddl-auto=update` permite que Hibernate cree o actualice las tablas durante el arranque. Es comodo para desarrollo, pero mas adelante se podria cambiar por migraciones con Flyway o Liquibase.

La clave JWT de desarrollo funciona en local. Para un despliegue real, define `JWT_SECRET` como variable de entorno con un valor largo y privado.
