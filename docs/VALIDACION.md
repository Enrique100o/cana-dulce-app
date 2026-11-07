# Validación de Caña Dulce

## Entrega final

Aplicación 1.2.3, código de versión 6. Comprobaciones registradas el 6 de noviembre
de 2026. Los resultados y las capturas están en el paquete de
[Entrega final](https://github.com/Enrique100o/cana-dulce-app/releases/tag/entrega-final).

| Comprobación | Resultado y alcance |
|---|---|
| Backend general | 58 comprobaciones aprobadas en MySQL temporal: entradas, roles, CRUD, filtros, concurrencia, calificación e idempotencia. |
| Respuestas por pregunta | 50 comprobaciones aprobadas: acierto/error, propiedad del intento, confirmación inmutable, solicitudes concurrentes, recuperación y finalización compatible. |
| Eliminación de lecturas | 53 comprobaciones registradas durante la mejora docente: confirmación, borrado sin intentos, conservación del historial y concurrencia. |
| Android | Tres recorridos aprobados en emulador para registro y sesiones, misión perfecta, reanudación, aciertos y errores entre preguntas y resultado final. Se conserva además el registro del recorrido docente. |
| Compilación | `assembleRelease` y `lintDebug` correctos; Lint sin errores. Quedan avisos de dependencias y reglas de respaldo. |
| APK | Firma y alineación verificadas. Instalación y arranque correctos en el emulador. |
| Nube | `/health` respondió 200 con MySQL conectado y la ruta de confirmación aplicó el control de rol esperado. |

`Verificaciones.json`, dentro de `Evidencias_Finales.zip`, reúne los registros.
Los datos de integración se crearon en bases temporales separadas; los recorridos
locales no se ejecutaron sobre la base de estudiantes de Aiven.

La API activa en Render corresponde al cambio funcional `88202b1`. La organización
final modifica documentación y rutas de archivos, sin cambiar el funcionamiento
del backend ni del APK firmado.

## Sprint 1

La [publicación Sprint 1](https://github.com/Enrique100o/cana-dulce-app/releases/tag/sprint-1)
conserva el material de su evaluación. Sus resultados incluyen 58 comprobaciones
del backend, 27 solicitudes y 47 aserciones Postman contra la nube, dos pruebas
Android y persistencia tras reiniciar Render.

El Sprint 1 acredita HU-01 a HU-05 y HU-12. La funcionalidad posterior está acumulada
en `main`; los documentos históricos describen la versión evaluada en ese momento.

## Repetir la comprobación HTTP

Importa `docs/CanaDulce.postman_collection.json` y configura las credenciales como
variables locales. Ejecuta las solicitudes en orden, usando un alias de prueba nuevo.
La colección crea registros para comprobar el recorrido y conserva su historial.
El contrato y los estados esperados se explican en `docs/API.md`.

## Repetir las pruebas Android

Desde `android`, compila `:app:assembleDebug :app:assembleDebugAndroidTest`.
El ejecutor es `androidx.test.runner.AndroidJUnitRunner`. Proporciona `aliasNuevo`
único, `claveNueva`, `usuarioDocente` y `claveDocente` como argumentos de instrumentación.
El código no contiene credenciales reales.

- `FlujosSprintUnoTest`: acceso, perfil, búsqueda, lectura, sesión y falta de conexión.
- `FlujosSprintDosTest`: recuperación, reacciones por pregunta, resultado, insignias
  y gestión docente.
- `ReaccionesPersonajeTest`: estados visuales, reproducción y pausa del personaje.

Los recorridos dependen de sus lecturas de demostración. Usa un entorno de pruebas
con la biblioteca espacial y “La semilla de Sofía”. Para conectar el emulador a una
API local aislada se admite `-PapiLocal=http://10.0.2.2:PUERTO/` solo en desarrollo;
el APK de distribución usa HTTPS en Render.

La comprobación de desconexión restaura las opciones de red al terminar. Las capturas
se guardan en la carpeta `evidencias` del almacenamiento externo privado de la aplicación.

## Pendientes de entrega

- Realizar al final la validación con el cliente inicial e incorporar evidencia real
  y sus observaciones. Las capturas del emulador no acreditan esa validación.
- Comprobar la instalación y el recorrido en un teléfono físico cuando esté disponible.
- Verificar el estado final de las historias y el acceso de la profesora al tablero.

No se marca ninguno de estos puntos como completado por la sola existencia del código.
