# Caña Dulce

Aplicación de comprensión lectora para estudiantes de tercero y cuarto. Esta versión
reconstruye el proyecto por historias de usuario y separa la aplicación Android de la API.

## Servicios publicados

- API: https://cana-dulce-api.onrender.com
- Comprobación de conexión: https://cana-dulce-api.onrender.com/health
- Repositorio: https://github.com/Enrique100o/cana-dulce-app

La dirección `/health` devuelve JSON con el estado del servidor y su conexión a MySQL.

## Estructura

- `backend/src`: servidor Express, rutas, validaciones, sesiones y acceso a MySQL.
- `backend/database/schema.sql`: estructura de las seis tablas.
- `backend/database/migrar-datos.js`: utilidad utilizada para trasladar los datos locales.
- `backend/certificados/aiven-ca.pem`: certificado público para verificar MySQL en Aiven.
- `android`: aplicación Kotlin con Jetpack Compose y conexión HTTPS a Render.
- `backend/docs/API.md`: contrato de las 19 operaciones de negocio.
- `backend/postman/CanaDulce.postman_collection.json`: colección ordenada de pruebas HTTP.
- `render.yaml`: configuración de despliegue del backend.

## Ejecutar el backend

Requisitos: Node.js 24 y acceso a la base MySQL del proyecto.

1. Entra en `backend` y ejecuta `npm ci`.
2. Copia `.env.example` como `.env` y completa los campos privados.
3. Ejecuta `npm run dev` para desarrollo o `npm start` para ejecución normal.
4. Consulta `http://localhost:3000/health`. Debe responder HTTP 200 y
   `baseDeDatos: "conectada"`.

El archivo `.env` se guarda únicamente en el equipo. No se publica en GitHub.
Para ejecutar en otro computador se necesitan sus valores privados; clonar el repositorio
por sí solo no configura las credenciales.

## Base de datos

La API usa MySQL en Aiven mediante TLS, verifica la autoridad certificadora y el nombre
del servidor. El usuario de aplicación tiene permisos de lectura y escritura sobre las
seis tablas, sin permisos para administrar usuarios o modificar la estructura.

Las tablas son `ESTUDIANTE`, `LECTURA`, `PREGUNTA`, `OPCION`, `INTENTO` y `RESPUESTA`.
Los nombres en mayúsculas se conservan porque el servidor diferencia mayúsculas y minúsculas.
Los códigos de acceso se almacenan como hashes scrypt y las sesiones utilizan JWT.

La migración inicial ya fue realizada. La utilidad de migración requiere una configuración
separada con `DB_*` para el origen y `CLOUD_DB_*` para el destino. No debe ejecutarse con
el `.env` actual de la aplicación, que ya apunta a Aiven. El destino debe estar vacío;
la utilidad rechaza tablas con datos para evitar sobrescrituras.

## Despliegue en Render

Crear un Web Service con esta configuración:

| Campo | Valor |
|---|---|
| Directorio raíz | `backend` |
| Entorno | Node |
| Instalación | `npm ci --omit=dev` |
| Inicio | `npm start` |
| Health check | `/health` |
| Plan | Free |

Configurar `NODE_ENV=production`, las cinco variables `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USER`, `DB_PASSWORD`, además de `DB_SSL=true`,
`DB_SSL_CA_PATH=certificados/aiven-ca.pem`, `JWT_SECRET`, `DOCENTE_USUARIO` y
`DOCENTE_PASSWORD_HASH`. Render asigna `PORT` automáticamente.

También se puede utilizar `render.yaml`. Las credenciales se introducen en Render;
el archivo de configuración no contiene sus valores.

El plan gratuito puede suspender la API por inactividad. La primera consulta posterior
puede tardar mientras el servicio vuelve a iniciar.

## Funciones disponibles

Registro e ingreso de estudiantes, ingreso docente, sesión actual y permisos por rol;
creación, consulta, actualización, publicación y desactivación de lecturas; creación de
preguntas con opciones y consulta del cuestionario. La consulta estudiantil oculta las
respuestas correctas.

El backend también incluye edición y desactivación de preguntas, historial e inicio de
intentos, entrega de respuestas, calificación, estrellas, progreso e insignias. El servidor
calcula los resultados y protege la edición de actividades que ya tienen intentos.

## Aplicación Android - Sprint 1

Incluye HU-01 a HU-05: registro, inicio y cierre de sesión estudiantil, acceso docente,
misiones filtradas por grado con búsqueda y lectura con tamaño de texto ajustable.
La sesión se cifra con Android Keystore y se valida al reabrir la aplicación. Los códigos
de acceso no se guardan en el teléfono. El área docente permite consultar lecturas y estudiantes.

1. Instala Android Studio con JDK 25 y Android SDK 37. El proyecto usa Gradle 9.6 y AGP 9.4.
2. Clona el repositorio y abre la carpeta `android` como proyecto en Android Studio.
3. Deja terminar la sincronización de Gradle y ejecuta `app` en un teléfono o emulador
   con Android 7.0 (API 24) o posterior y conexión a Internet.
4. La URL pública ya está configurada en `android/app/build.gradle.kts`. Para utilizar
   la API publicada no necesitas instalar MySQL ni iniciar Node.js en ese computador.

Desde PowerShell, en `android`, `./gradlew.bat :app:assembleDebug` genera el APK en
`app/build/outputs/apk/debug/app-debug.apk`. Es una compilación de desarrollo para la
sustentación; cada computador puede generar una firma de depuración distinta.

Los cuestionarios, resultados, insignias y administración mediante pantallas Android
corresponden al Sprint 2. Sus operaciones del backend ya están implementadas.

## Verificación

La validación del backend cubre registros, roles, filtros, CRUD, intentos concurrentes,
calificación, idempotencia y conservación del historial. La colección de Postman incluye
comprobaciones de estado HTTP, permisos, estructura y resultados. Introduce las claves
en variables locales de Postman; la colección publicada no contiene credenciales reales.

En Android, `FlujosSprintUnoTest` comprueba registro, duplicados, accesos inválidos y
válidos, búsqueda, lectura, conservación de tamaño, recuperación de sesión y cierre.
Recibe credenciales mediante argumentos del ejecutor de pruebas; no están en el código.

El servicio actual de Render usa despliegue manual: después de publicar un commit se
selecciona **Manual Deploy > Deploy latest commit** en el panel. Un `git push` por sí solo
no cambia la versión activa.
