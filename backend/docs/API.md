# Contrato de la API de Caña Dulce

Base pública: `https://cana-dulce-api.onrender.com`.
Formato: JSON UTF-8. Fechas: UTC ISO 8601. IDs: enteros positivos.
Las rutas privadas reciben `Authorization: Bearer <token>`.
Las sesiones caducan después de 8 horas. El cierre en Android elimina la copia local
del token; no existe una lista de revocación de JWT en el servidor.

## Operaciones

| N.º | Método y ruta | Permiso | Respuesta normal |
|---|---|---|---|
| 1 | POST `/api/estudiantes` | Público | 201, perfil sin código ni hash |
| 2 | POST `/api/sesiones/estudiante` | Público | 200, token, rol y perfil |
| 3 | POST `/api/sesiones/docente` | Público | 200, token y rol |
| 4 | GET `/api/estudiantes` | Docente | 200, `{datos,total}` |
| 5 | POST `/api/lecturas` | Docente | 201, lectura INACTIVA |
| 6 | GET `/api/lecturas` | Autenticado | 200, `{datos,total}` |
| 7 | GET `/api/lecturas/:id` | Autenticado con acceso | 200, lectura con contenido |
| 8 | PUT `/api/lecturas/:id` | Docente | 200, lectura actualizada |
| 9 | DELETE `/api/lecturas/:id` | Docente | 204, desactivación lógica |
| 10 | POST `/api/lecturas/:id/preguntas` | Docente | 201, pregunta y opciones |
| 11 | GET `/api/lecturas/:id/preguntas` | Autenticado con acceso | 200, preguntas sin claves |
| 12 | GET `/api/docente/lecturas/:id/preguntas` | Docente | 200, preguntas con claves |
| 13 | PUT `/api/preguntas/:id` | Docente | 200, pregunta actualizada |
| 14 | DELETE `/api/preguntas/:id` | Docente | 204, desactivación lógica |
| 15 | POST `/api/intentos` | Estudiante | 201, intento EN_PROGRESO |
| 16 | GET `/api/intentos` | Propietario o docente | 200, `{datos,total}` |
| 17 | GET `/api/intentos/:id` | Propietario o docente | 200, intento y resultado si existe |
| 18 | POST `/api/intentos/:id/finalizar` | Estudiante propietario | 200, resultado y respuestas |
| 19 | GET `/api/estudiantes/:id/progreso` | Propietario o docente | 200, resumen e insignias |
| 20 | DELETE `/api/lecturas/:id/eliminar` | Docente | 204, eliminación de una lectura sin intentos |
| 21 | POST `/api/intentos/:id/respuestas` | Estudiante propietario | 200, respuesta confirmada y `esCorrecta` |

Rutas auxiliares: GET `/health` comprueba API y MySQL; GET `/api/sesiones/actual`
valida la sesión. No se reciben cuerpos en consultas GET ni en DELETE.

## Registro y sesiones

Registro: `{"alias":"LunaLectora","grado":3,"codigoAcceso":"una clave privada"}`.
Alias: 3 a 30 caracteres después de recortar y normalizar a NFC. Grado: 3 o 4.
Código: 6 a 32 caracteres. El alias normalizado es único sin distinguir mayúsculas.
El código se almacena como hash scrypt con sal aleatoria; nunca se devuelve.

Ingreso estudiantil: `{"alias":"LunaLectora","codigoAcceso":"una clave privada"}`.
Ingreso docente: `{"usuario":"docente","contrasena":"su clave privada"}`.
El docente se configura con variables privadas del servidor; no existe registro público
de docentes. Credenciales incorrectas devuelven 401 sin indicar qué campo falló.

## Lecturas, preguntas y publicación

Crear lectura:

```json
{
  "titulo": "La semilla del jardín",
  "contenido": "Una niña sembró una semilla y la cuidó cada mañana hasta que nació una planta.",
  "grado": 3,
  "dificultad": "BAJA",
  "categoria": "Naturaleza"
}
```

Título: 3-120 caracteres; contenido: 20-8000; categoría opcional hasta 50.
Dificultad: BAJA, MEDIA o ALTA. Siempre se crea INACTIVA.
PUT usa esos mismos campos y además `estado` ACTIVA o INACTIVA.
Para publicar debe existir al menos una pregunta activa válida.

Crear pregunta:

```json
{
  "enunciado": "¿Qué sembró la niña?",
  "tipo": "LITERAL",
  "orden": 1,
  "opciones": [
    {"texto":"Una semilla","orden":1,"esCorrecta":true},
    {"texto":"Una piedra","orden":2,"esCorrecta":false}
  ]
}
```

Enunciado: 5-400 caracteres. Tipo: LITERAL, INFERENCIAL, IDEA_PRINCIPAL,
VOCABULARIO o SECUENCIA. Orden: 1-999, único dentro de la lectura.
Entre 2 y 4 opciones; texto de 1-250 caracteres, orden único 1-4 y exactamente
una correcta. PUT exige el objeto completo y `estado`; reemplaza las opciones.
La consulta estudiantil no contiene `esCorrecta`.

Después del primer intento la lectura, sus preguntas y opciones quedan bloqueadas
para edición. Puede desactivarse la lectura: deja de admitir nuevos intentos, pero
los pendientes pueden finalizar y el historial continúa disponible.
La desactivación de preguntas también respeta las reglas de publicación e historial.

La eliminación permanente requiere confirmación en Android. Borra la lectura y sus
preguntas y opciones dentro de una transacción. Si tiene cualquier intento, devuelve
409 y conserva todos los registros. Usa el mismo bloqueo de lectura que el inicio de
intentos y la edición de preguntas. Repetir una eliminación ya completada devuelve 204.

## Filtros y autorización

- Lecturas: `grado`, `estado`, `q` (búsqueda de título, hasta 100 caracteres).
  El estudiante solo consulta actividades publicables ACTIVAS de su propio grado;
  el servidor toma el grado del perfil, no confía en el teléfono.
- Estudiantes, solo docente: `grado`, `estado` ACTIVO/INACTIVO y `q` por alias.
- Preguntas administrativas: `estado` ACTIVA/INACTIVA, opcional.
- Intentos: `estudianteId`, `idLectura`, `estado` EN_PROGRESO/COMPLETADO.
  El estudiante siempre queda limitado a sus propios intentos.
- Progreso e intento individual: propietario o docente. No se aceptan identidades
  ni puntajes enviados por el cliente como reemplazo de la sesión.

## Intentos, calificación y progreso

Iniciar: `{"idLectura":1}`. Solo se permite un intento pendiente por estudiante
y lectura. Otro inicio concurrente devuelve 409 con `idIntentoExistente`.

Confirmar una pregunta envía `{"idPregunta":1,"idOpcion":2}` a
`POST /api/intentos/:id/respuestas`. La API guarda la primera elección y devuelve
la corrección de esa opción para mostrar la animación antes de continuar. Repetir
la misma elección devuelve la confirmación guardada. Intentar cambiarla devuelve
409. Consultar el intento pendiente recupera esas respuestas confirmadas.

Finalizar:

```json
{"respuestas":[{"idPregunta":1,"idOpcion":1}]}
```

Debe incluir exactamente una opción válida por pregunta activa. Una opción debe
pertenecer a la pregunta indicada. Toda la entrega y el resultado se guardan en una
transacción. El servidor calcula `puntaje = correctas / total * 100`, redondeado a dos
decimales. Enviar la misma selección otra vez devuelve el resultado guardado; cambiar
las respuestas de un intento completado devuelve 409.
Las respuestas confirmadas durante el recorrido también se conservan al finalizar.
El endpoint de finalización acepta las entregas completas de versiones anteriores.

Estrellas: 0 puntos = 0; más de 0 y menos de 50 = 1; de 50 a menos de 80 = 2;
80 o más = 3. El intento pendiente tiene puntaje y estrellas nulos.

El progreso cuenta solo intentos completados: número de intentos, lecturas distintas,
promedio, mejor puntaje, suma de puntos y estrellas. Insignias: Primera aventura
(1 lectura distinta), Explorador de historias (3 distintas), Lectura brillante (100 puntos).
Reintentar una lectura completada puede sumar un nuevo resultado; reenviar el mismo
intento nunca duplica puntos. Las pantallas de estas funciones corresponden al Sprint 2.

## Errores

Formato: `{"estado":"error","mensaje":"Explicación para el usuario"}`.

| HTTP | Significado |
|---|---|
| 400 | Datos, identificadores, filtros o JSON inválidos |
| 401 | Sesión ausente, vencida o credenciales incorrectas |
| 403 | Rol o propietario sin permiso |
| 404 | Recurso inexistente o lectura no disponible para ese estudiante |
| 409 | Alias duplicado, intento pendiente o actividad protegida por historial |
| 413 | Cuerpo JSON demasiado grande |
| 500/503 | Fallo interno o indisponibilidad temporal sin revelar SQL ni credenciales |

## Usar la colección de Postman

Importa `postman/CanaDulce.postman_collection.json`. Define localmente `claveDocente` y
`codigoPrueba` (6-32 caracteres), sin sincronizar contraseñas ni exportar tokens.
El ejecutor genera un alias único y una lectura propia; recorre el ciclo completo con
aserciones. Las peticiones crean datos de demostración y desactivan su lectura al final.
No eliminan perfiles ni historial, porque la API conserva esos registros.

La colección muestra dos variantes de DELETE de pregunta: 204 antes de utilizar la
actividad y 409 cuando ya tiene intentos. Para una prueba nueva se debe ejecutar desde
el inicio. El servidor puede tardar en arrancar tras inactividad en Render Free.
