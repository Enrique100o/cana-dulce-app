# Caña Dulce

Aplicación de comprensión lectora para estudiantes de tercero y cuarto. Esta versión
reconstruye el proyecto por historias de usuario y separa la aplicación Android de la API.

## Estructura

- `backend/src`: servidor Express, rutas, validaciones, sesiones y acceso a MySQL.
- `backend/database/schema.sql`: estructura de las seis tablas.
- `backend/database/migrar-datos.js`: utilidad utilizada para trasladar los datos locales.
- `backend/certificados/aiven-ca.pem`: certificado público para verificar MySQL en Aiven.
- `android`: carpeta reservada para la aplicación Kotlin; todavía no está inicializada.
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

Quedan pendientes la edición y desactivación de preguntas, los intentos de lectura,
la entrega y calificación de respuestas, la consulta de progreso y la aplicación Android.
El primer despliegue corresponde a una versión funcional del backend y no acredita el
cierre de todas las historias de usuario.
