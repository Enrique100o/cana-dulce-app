# Validación del Sprint 1

Fecha: 2 de noviembre de 2026. Aplicación: Caña Dulce.

## Resultado comprobado

- Backend: 58 comprobaciones HTTP y de integración correctas en una base MySQL temporal
  independiente, con usuario de permisos limitados. Incluye concurrencia, transacciones,
  roles, filtros, edición protegida, respuestas y entrega idempotente.
- Cálculo: 13 casos adicionales de redondeo y límites de estrellas correctos.
- Colección publicada: 27 solicitudes contra Render, 47 aserciones correctas, cero fallos.
  Recorre las 19 operaciones de negocio y las rutas auxiliares de salud y sesión.
- Android: dos pruebas instrumentadas correctas en Pixel 10 Pro, Android API 36.
  Registro, duplicados, accesos inválidos y válidos, cierre y recuperación de sesión,
  búsqueda, ajuste de texto, lectura/cuestionario con selecciones conservadas, giro de
  pantalla, resultado del servidor y mensaje de falta de Internet.
- Compilación del APK y Android Lint correctos: cero errores. Quedan tres avisos de
  versiones de dependencias más recientes; no se actualizaron las herramientas del curso.
- Persistencia: el intento de demostración mantiene respuestas, 100 puntos y tres
  estrellas después de reiniciar Render. MySQL se encuentra en Aiven y no depende
  del disco de la instancia del backend.

## Repetir la comprobación HTTP

Importa `postman/CanaDulce.postman_collection.json` y configura las claves en variables
locales. Ejecuta las peticiones en orden. La colección crea un perfil y una lectura propios,
desactiva la lectura al terminar y conserva su historial. El contrato y los estados HTTP
esperados se explican en `docs/API.md`.

## Repetir las pruebas Android

Compila la aplicación y las pruebas con `:app:assembleDebug :app:assembleAndroidTest`.
El ejecutor es `androidx.test.runner.AndroidJUnitRunner`; clase
`com.canadulce.app.FlujosSprintUnoTest`. Proporciona `aliasNuevo` único, `claveNueva`,
`usuarioDocente` y `claveDocente` como argumentos de instrumentación. El alias del
perfil de prueba debe ser nuevo en cada ejecución. La prueba usa la lectura de demostración
“La semilla de Sofía”; no se deben cambiar esos datos mientras se verifica ese flujo.

La comprobación de desconexión desactiva temporalmente Wi-Fi y datos del emulador de
pruebas y recupera sus valores anteriores mediante `finally`. Las capturas quedan en
el directorio externo privado de la aplicación, carpeta `evidencias`.

## Alcance

Estas evidencias sustentan HU-01 a HU-05 y HU-12. El enlace entre lectura y cuestionario
y su resultado básico ya funciona. El cierre de las historias del Sprint 2 se verifica
por separado frente a sus propios criterios; no se acredita por la sola existencia de APIs.

Los informes y las capturas de la entrega se distribuyen como archivos de la versión
del Sprint 1 para conservar el código fuente separado de los documentos.
