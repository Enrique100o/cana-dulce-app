package com.canadulce.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.canadulce.app.datos.*
import java.net.URLEncoder
import java.text.Normalizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class PrincipalViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val api = ApiCanaDulce()
    private val almacen = AlmacenSesion(aplicacion)
    private val preferencias = aplicacion.getSharedPreferences("lectura", 0)
    private val borradores = BorradorCuestionario(aplicacion)
    private var trabajo: Job? = null
    private var operacionActual = 0
    var estado by
        mutableStateOf(
            EstadoApp(
                tamanoTexto = preferencias.getInt("tamano", 20).coerceIn(18, 32),
                movimiento = preferencias.getBoolean("movimiento", true),
            ),
        )
        private set

    init {
        almacen.leer()?.let { sesion ->
            estado = estado.copy(sesion = sesion, pantalla = Pantalla.RECUPERAR)
            recuperar()
        }
    }

    private fun normalizar(texto: String) = Normalizer.normalize(texto.trim(), Normalizer.Form.NFC)

    private fun longitud(texto: String) = texto.codePointCount(0, texto.length)

    private fun error(mensaje: String) {
        estado = estado.copy(error = mensaje, aviso = null)
    }

    private fun ejecutar(operacion: suspend () -> Unit) {
        val numeroOperacion = ++operacionActual
        trabajo?.cancel()
        trabajo = viewModelScope.launch {
            estado = estado.copy(cargando = true, error = null)
            try {
                operacion()
            } catch (cancelada: CancellationException) {
                throw cancelada
            } catch (fallo: ErrorApi) {
                val sesionTerminada = fallo.estado == 401 ||
                    (fallo.estado == 403 && estado.pantalla == Pantalla.RECUPERAR)
                if (sesionTerminada && estado.sesion != null) {
                    val docente = estado.sesion?.rol == "DOCENTE"
                    almacen.borrar()
                    estado =
                        EstadoApp(
                            pantalla = if (docente) Pantalla.DOCENTE else Pantalla.ESTUDIANTE,
                            error = "Tu sesión terminó. Vuelve a ingresar.",
                            tamanoTexto = estado.tamanoTexto,
                            movimiento = estado.movimiento,
                        )
                } else error(fallo.message)
            } catch (_: Exception) {
                error("No pudimos completar la acción. Vuelve a intentarlo.")
            } finally {
                if (numeroOperacion == operacionActual) estado = estado.copy(cargando = false)
            }
        }
    }

    fun ir(pantalla: Pantalla) {
        operacionActual++
        trabajo?.cancel()
        estado = estado.copy(pantalla = pantalla, error = null, aviso = null, cargando = false)
    }

    fun quitarMensaje() {
        estado = estado.copy(error = null, aviso = null)
    }

    fun registrar(alias: String, grado: Int?, codigo: String) {
        val limpio = normalizar(alias)
        if (longitud(limpio) !in 3..30) return error("Tu alias debe tener entre 3 y 30 caracteres.")
        if (grado !in listOf(3, 4)) return error("Selecciona tercero o cuarto.")
        if (longitud(codigo) !in 6..32)
            return error("El código debe tener entre 6 y 32 caracteres.")
        ejecutar {
            api.solicitar(
                "api/estudiantes",
                "POST",
                JSONObject().put("alias", limpio).put("grado", grado).put("codigoAcceso", codigo),
            )
            estado = estado.copy(aliasSugerido = limpio)
            try {
                guardarIngreso(
                    api.solicitar(
                        "api/sesiones/estudiante",
                        "POST",
                        JSONObject().put("alias", limpio).put("codigoAcceso", codigo),
                    ),
                )
            } catch (fallo: ErrorApi) {
                estado =
                    estado.copy(
                        pantalla = Pantalla.ESTUDIANTE,
                        aviso = "Tu perfil ya está creado. Ingresa con tu alias y código.",
                    )
                throw fallo
            }
        }
    }

    fun ingresarEstudiante(alias: String, codigo: String) {
        val limpio = normalizar(alias)
        if (longitud(limpio) !in 3..30 || longitud(codigo) !in 6..32) {
            return error("Escribe un alias de 3 a 30 caracteres y un código de 6 a 32.")
        }
        ejecutar {
            guardarIngreso(
                api.solicitar(
                    "api/sesiones/estudiante",
                    "POST",
                    JSONObject().put("alias", limpio).put("codigoAcceso", codigo),
                ),
            )
        }
    }

    fun ingresarDocente(usuario: String, contrasena: String) {
        val limpio = normalizar(usuario)
        if (longitud(limpio) !in 1..50 || longitud(contrasena) !in 1..128) {
            return error("Escribe tu usuario y contraseña.")
        }
        ejecutar {
            guardarIngreso(
                api.solicitar(
                    "api/sesiones/docente",
                    "POST",
                    JSONObject().put("usuario", limpio).put("contrasena", contrasena),
                ),
            )
        }
    }

    private suspend fun guardarIngreso(json: JSONObject) {
        val sesion = Sesion.desdeJson(json)
        almacen.guardar(sesion)
        estado =
            estado.copy(
                sesion = sesion,
                busqueda = "",
                lectura = null,
                pantalla =
                    if (sesion.rol == "DOCENTE") Pantalla.PANEL_DOCENTE else Pantalla.MISIONES,
            )
        cargarDatos("", incluirProgreso = true)
    }

    fun recuperar() = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val json = api.solicitar("api/sesiones/actual", token = sesion.token)
        val actual =
            sesion.copy(
                rol = json.getString("rol"),
                estudiante = json.optJSONObject("estudiante")?.let(Perfil::desdeJson),
            )
        almacen.guardar(actual)
        estado =
            estado.copy(
                sesion = actual,
                pantalla =
                    if (actual.rol == "DOCENTE") Pantalla.PANEL_DOCENTE else Pantalla.MISIONES,
            )
        cargarDatos(estado.busqueda, incluirProgreso = true)
    }

    private suspend fun cargarDatos(busqueda: String, incluirProgreso: Boolean = false) {
        val sesion = estado.sesion ?: return
        val filtro =
            busqueda
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let { "?q=" + URLEncoder.encode(it, "UTF-8") }
                .orEmpty()
        val json = api.solicitar("api/lecturas$filtro", token = sesion.token)
        estado = estado.copy(lecturas = json.getJSONArray("datos").leer(Lectura::desdeJson))
        if (incluirProgreso && sesion.estudiante != null) {
            val pendientes = listaIntentos("estado=EN_PROGRESO", sesion.token)
            estado = estado.copy(pendientes = pendientes)
            cargarProgreso(sesion.estudiante.idEstudiante, sesion.token)
        }
    }

    fun buscar(texto: String) {
        if (longitud(texto) > 100) return
        estado = estado.copy(busqueda = texto)
        ejecutar {
            delay(350)
            cargarDatos(texto)
        }
    }

    fun refrescar() = ejecutar {
        if (estado.sesion?.rol == "DOCENTE" && estado.gestion.listaEstudiantes) cargarEstudiantes()
        else cargarDatos(estado.busqueda, incluirProgreso = true)
    }

    fun abrirLectura(id: Int) = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val lectura = Lectura.desdeJson(api.solicitar("api/lecturas/$id", token = sesion.token))
        if (sesion.rol == "DOCENTE") {
            val preguntas = preguntasDocente(lectura.id, sesion.token)
            estado = estado.copy(
                lectura = lectura,
                pantalla = Pantalla.GESTION_LECTURA,
                gestion = estado.gestion.copy(preguntas = preguntas),
            )
            return@ejecutar
        }
        estado =
            estado.copy(
                lectura = lectura,
                pantalla = Pantalla.LECTURA,
                regresarAlCuestionario = false,
                preguntas = if (estado.lectura?.id == id) estado.preguntas else emptyList(),
                selecciones = if (estado.lectura?.id == id) estado.selecciones else emptyMap(),
                idIntento = if (estado.lectura?.id == id) estado.idIntento else null,
                resultado = if (estado.lectura?.id == id) estado.resultado else null,
                pasoPregunta = if (estado.lectura?.id == id) estado.pasoPregunta else 0,
            )
    }

    fun abrirCuestionario() {
        if (estado.idIntento != null && estado.resultado == null && estado.preguntas.isNotEmpty()) {
            ir(Pantalla.CUESTIONARIO)
            return
        }
        ejecutar {
            val sesion = estado.sesion ?: return@ejecutar
            val lectura = estado.lectura ?: return@ejecutar
            val preguntas = preguntasEstudiante(lectura.id, sesion.token)
            val filtro = "idLectura=${lectura.id}&estado=EN_PROGRESO"
            val intento =
                listaIntentos(filtro, sesion.token).firstOrNull()
                    ?: try {
                        Intento.desdeJson(api.solicitar(
                                "api/intentos",
                                "POST",
                                JSONObject().put("idLectura", lectura.id),
                                sesion.token,
                            ))
                    } catch (fallo: ErrorApi) {
                        if (fallo.estado != 409) throw fallo
                        listaIntentos(filtro, sesion.token).firstOrNull() ?: throw fallo
                    }
            mostrarCuestionario(sesion, lectura, intento, preguntas)
        }
    }

    private suspend fun preguntasEstudiante(id: Int, token: String): List<Pregunta> {
        val preguntas = api.solicitar("api/lecturas/$id/preguntas", token = token)
            .getJSONArray("datos").leer(Pregunta::desdeJson)
        if (preguntas.isEmpty()) throw ErrorApi(409, "Esta historia todavía no tiene preguntas.")
        return preguntas
    }

    private fun mostrarCuestionario(
        sesion: Sesion, lectura: Lectura, intento: Intento, preguntas: List<Pregunta>,
    ) {
        val estudiante = sesion.estudiante ?: return
        val (paso, selecciones) = borradores.leer(estudiante.idEstudiante, intento.id, preguntas)
        estado = estado.copy(
            pantalla = Pantalla.CUESTIONARIO,
            lectura = lectura,
            preguntas = preguntas,
            idIntento = intento.id,
            selecciones = selecciones,
            pasoPregunta = paso,
            resultado = null,
            regresarAlCuestionario = false,
            pendientes = listOf(intento) + estado.pendientes.filterNot { it.id == intento.id },
        )
    }

    fun retomarIntento(intento: Intento) = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val actual = api.solicitar("api/intentos/${intento.id}", token = sesion.token)
        if (actual.getString("estado") == "COMPLETADO") {
            mostrarResultado(actual, Pantalla.MISIONES)
            return@ejecutar
        }
        val lectura = Lectura.desdeJson(
            api.solicitar("api/lecturas/${intento.lectura}", token = sesion.token),
        )
        mostrarCuestionario(sesion, lectura, intento, preguntasEstudiante(lectura.id, sesion.token))
    }

    fun elegirRespuesta(idPregunta: Int, idOpcion: Int) {
        val pregunta = estado.preguntas.getOrNull(estado.pasoPregunta) ?: return
        if (estado.cargando || pregunta.id != idPregunta || pregunta.opciones.none { it.id == idOpcion })
            return
        estado = estado.copy(selecciones = estado.selecciones + (idPregunta to idOpcion), error = null)
        guardarBorrador()
    }

    fun irAPregunta(indice: Int) {
        if (estado.cargando || indice !in estado.preguntas.indices) return
        estado = estado.copy(pasoPregunta = indice, error = null)
        guardarBorrador()
    }

    private fun guardarBorrador() {
        val estudiante = estado.sesion?.estudiante ?: return
        val intento = estado.idIntento ?: return
        borradores.guardar(estudiante.idEstudiante, intento, estado.pasoPregunta, estado.selecciones)
    }

    fun releer() {
        ir(Pantalla.LECTURA)
        estado = estado.copy(regresarAlCuestionario = true)
    }

    fun finalizarMision() {
        if (estado.preguntas.isEmpty() || estado.selecciones.size != estado.preguntas.size) {
            return error("Elige una respuesta para cada pregunta antes de finalizar.")
        }
        ejecutar {
            val sesion = estado.sesion ?: return@ejecutar
            val id = estado.idIntento ?: return@ejecutar
            val respuestas = JSONArray()
            estado.preguntas.forEach { pregunta ->
                respuestas.put(
                    JSONObject()
                        .put("idPregunta", pregunta.id)
                        .put("idOpcion", estado.selecciones.getValue(pregunta.id)),
                )
            }
            val resultado =
                api.solicitar(
                    "api/intentos/$id/finalizar",
                    "POST",
                    JSONObject().put("respuestas", respuestas),
                    sesion.token,
                )
            mostrarResultado(resultado, Pantalla.MISIONES)
        }
    }

    private suspend fun mostrarResultado(json: JSONObject, regreso: Pantalla) {
        val resultado = Resultado.desdeJson(json)
        val id = json.getInt("idIntento")
        val sesion = estado.sesion
        val estudiante = sesion?.estudiante
        val insigniasPrevias = estado.progreso?.insignias
        estudiante?.let { borradores.borrar(it.idEstudiante, id) }
        estado = estado.copy(
            pantalla = Pantalla.RESULTADO,
            resultado = resultado,
            regresoResultado = regreso,
            premiosNuevos = emptyList(),
            idIntento = null,
            selecciones = emptyMap(),
            pendientes = estado.pendientes.filterNot { it.id == id },
            regresarAlCuestionario = false,
        )
        if (regreso == Pantalla.MISIONES && estudiante != null && insigniasPrevias != null) {
            try {
                cargarProgreso(estudiante.idEstudiante, sesion.token)
                val previas = insigniasPrevias.filter { it.obtenida }.map { it.codigo }.toSet()
                val nuevas = estado.progreso?.insignias.orEmpty().filter { it.obtenida && it.codigo !in previas }
                estado = estado.copy(premiosNuevos = nuevas)
            } catch (_: ErrorApi) {
                // El resultado ya está guardado. La bitácora puede volver a consultar las insignias.
            }
        }
    }

    private suspend fun listaIntentos(filtros: String, token: String): List<Intento> =
        api.solicitar("api/intentos?$filtros", token = token)
            .getJSONArray("datos").leer(Intento::desdeJson)

    private suspend fun cargarProgreso(id: Int, token: String) {
        val progreso = api.solicitar("api/estudiantes/$id/progreso", token = token)
        estado = estado.copy(progreso = Progreso.desdeJson(progreso))
    }

    fun abrirProgreso(perfil: Perfil? = null) {
        val sesion = estado.sesion ?: return
        val seleccionado = sesion.estudiante ?: perfil ?: return
        ir(Pantalla.PROGRESO)
        estado = estado.copy(
            progreso = null,
            historial = emptyList(),
            gestion = estado.gestion.copy(estudiante = seleccionado),
        )
        refrescarProgreso()
    }

    fun refrescarProgreso() = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val perfil = estado.gestion.estudiante ?: return@ejecutar
        cargarProgreso(perfil.idEstudiante, sesion.token)
        val historial = listaIntentos("estudianteId=${perfil.idEstudiante}&estado=COMPLETADO", sesion.token)
        estado = estado.copy(historial = historial)
    }

    fun verResultado(intento: Intento) = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val json = api.solicitar("api/intentos/${intento.id}", token = sesion.token)
        mostrarResultado(json, Pantalla.PROGRESO)
    }

    fun alternarMovimiento() {
        val activar = !estado.movimiento
        estado = estado.copy(movimiento = activar)
        preferencias.edit { putBoolean("movimiento", activar) }
    }

    fun cambiarSeccionDocente(estudiantes: Boolean) {
        estado = estado.copy(
            gestion = estado.gestion.copy(listaEstudiantes = estudiantes),
            error = null,
        )
        refrescar()
    }

    fun filtrarEstudiantes(texto: String, grado: Int?, estadoPerfil: String?) {
        if (longitud(texto) > 100) return
        estado = estado.copy(
            gestion = estado.gestion.copy(busqueda = texto, grado = grado, estadoPerfil = estadoPerfil),
        )
        ejecutar {
            delay(300)
            cargarEstudiantes()
        }
    }

    private suspend fun cargarEstudiantes() {
        val sesion = estado.sesion ?: return
        val gestion = estado.gestion
        val filtros = buildList {
            gestion.busqueda.trim().takeIf { it.isNotEmpty() }?.let {
                add("q=" + URLEncoder.encode(it, "UTF-8"))
            }
            gestion.grado?.let { add("grado=$it") }
            gestion.estadoPerfil?.let { add("estado=$it") }
        }.joinToString("&")
        val lista = api.solicitar("api/estudiantes?$filtros", token = sesion.token)
            .getJSONArray("datos").leer(Perfil::desdeJson)
        estado = estado.copy(estudiantes = lista)
    }

    private suspend fun preguntasDocente(id: Int, token: String): List<Pregunta> =
        api.solicitar("api/docente/lecturas/$id/preguntas", token = token)
            .getJSONArray("datos").leer(Pregunta::desdeJson)

    fun editarLectura(nueva: Boolean) {
        if (nueva) {
            estado = estado.copy(
                lectura = null,
                gestion = estado.gestion.copy(preguntas = emptyList()),
            )
        }
        ir(Pantalla.EDITOR_LECTURA)
    }

    fun guardarLectura(edicion: EdicionLectura) {
        if (longitud(edicion.titulo.trim()) !in 3..120) return error("Escribe un título de 3 a 120 caracteres.")
        if (longitud(edicion.contenido.trim()) !in 20..8000)
            return error("El contenido debe tener entre 20 y 8000 caracteres.")
        if (longitud(edicion.categoria.trim()) > 50) return error("La categoría admite hasta 50 caracteres.")
        ejecutar {
            val sesion = estado.sesion ?: return@ejecutar
            val lectura = estado.lectura
            val datos = edicion.aJson()
            val respuesta = if (lectura == null) {
                api.solicitar("api/lecturas", "POST", datos, sesion.token)
            } else {
                api.solicitar(
                    "api/lecturas/${lectura.id}", "PUT",
                    datos.put("estado", lectura.estado), sesion.token,
                )
            }
            actualizarLecturaLocal(Lectura.desdeJson(respuesta))
            estado = estado.copy(pantalla = Pantalla.GESTION_LECTURA, aviso = "Lectura guardada.")
        }
    }

    fun cambiarPublicacion() = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val lectura = estado.lectura ?: return@ejecutar
        val ruta = "api/lecturas/${lectura.id}"
        val actualizada = if (lectura.estado == "ACTIVA") {
            api.solicitar(ruta, "DELETE", token = sesion.token)
            lectura.copy(estado = "INACTIVA")
        } else {
            val datos = EdicionLectura(
                lectura.titulo, lectura.contenido, lectura.grado, lectura.dificultad, lectura.categoria,
            ).aJson().put("estado", "ACTIVA")
            Lectura.desdeJson(api.solicitar(ruta, "PUT", datos, sesion.token))
        }
        actualizarLecturaLocal(actualizada)
        estado = estado.copy(
            aviso = if (actualizada.estado == "ACTIVA") "Misión publicada." else "Lectura desactivada.",
        )
    }

    private fun actualizarLecturaLocal(lectura: Lectura) {
        estado = estado.copy(
            lectura = lectura,
            lecturas = (estado.lecturas.filterNot { it.id == lectura.id } + lectura)
                .sortedBy { it.titulo.lowercase() },
        )
    }

    fun editarPregunta(pregunta: Pregunta?) {
        ir(Pantalla.EDITOR_PREGUNTA)
        estado = estado.copy(gestion = estado.gestion.copy(edicionPregunta = pregunta))
    }

    fun guardarPregunta(edicion: EdicionPregunta) {
        if (longitud(edicion.enunciado.trim()) !in 5..400)
            return error("El enunciado debe tener entre 5 y 400 caracteres.")
        if (edicion.orden !in 1..999) return error("Escribe un orden entre 1 y 999.")
        if (edicion.opciones.any { longitud(it.trim()) !in 1..250 })
            return error("Escribe entre 1 y 250 caracteres en cada opción.")
        if (edicion.correcta !in edicion.opciones.indices) return error("Marca la respuesta correcta.")
        ejecutar {
            val sesion = estado.sesion ?: return@ejecutar
            val lectura = estado.lectura ?: return@ejecutar
            val editada = estado.gestion.edicionPregunta
            val respuesta = if (editada == null) {
                api.solicitar(
                    "api/lecturas/${lectura.id}/preguntas", "POST", edicion.aJson(), sesion.token,
                )
            } else {
                api.solicitar(
                    "api/preguntas/${editada.id}", "PUT",
                    edicion.aJson().put("estado", edicion.estado), sesion.token,
                )
            }
            actualizarPreguntaLocal(Pregunta.desdeJson(respuesta))
            estado = estado.copy(pantalla = Pantalla.GESTION_LECTURA, aviso = "Pregunta guardada.")
        }
    }

    fun desactivarPregunta(pregunta: Pregunta) = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        api.solicitar("api/preguntas/${pregunta.id}", "DELETE", token = sesion.token)
        actualizarPreguntaLocal(pregunta.copy(estado = "INACTIVA"))
        estado = estado.copy(aviso = "Pregunta desactivada.")
    }

    private fun actualizarPreguntaLocal(pregunta: Pregunta) {
        val preguntas = (estado.gestion.preguntas.filterNot { it.id == pregunta.id } + pregunta)
            .sortedBy { it.orden }
        estado.lectura?.let {
            actualizarLecturaLocal(it.copy(totalPreguntas = preguntas.count { p -> p.estado == "ACTIVA" }))
        }
        estado = estado.copy(gestion = estado.gestion.copy(preguntas = preguntas))
    }

    fun cambiarTexto(cambio: Int) {
        val tamano = (estado.tamanoTexto + cambio).coerceIn(18, 32)
        estado = estado.copy(tamanoTexto = tamano)
        preferencias.edit { putInt("tamano", tamano) }
    }

    fun volver() {
        if (estado.pantalla == Pantalla.RESULTADO) {
            val regreso = estado.regresoResultado
            ir(regreso)
            if (regreso == Pantalla.MISIONES) refrescar()
            return
        }
        if (estado.pantalla == Pantalla.PROGRESO) {
            ir(if (estado.sesion?.rol == "DOCENTE") Pantalla.PANEL_DOCENTE else Pantalla.MISIONES)
            return
        }
        if (estado.pantalla == Pantalla.EDITOR_PREGUNTA ||
            (estado.pantalla == Pantalla.EDITOR_LECTURA && estado.lectura != null) ||
            (estado.pantalla == Pantalla.LECTURA && estado.sesion?.rol == "DOCENTE")) {
            ir(Pantalla.GESTION_LECTURA)
            return
        }
        if (estado.pantalla in listOf(Pantalla.GESTION_LECTURA, Pantalla.EDITOR_LECTURA)) {
            ir(Pantalla.PANEL_DOCENTE)
            return
        }
        if (estado.pantalla == Pantalla.LECTURA && estado.regresarAlCuestionario) {
            ir(Pantalla.CUESTIONARIO)
        } else if (
            estado.pantalla in listOf(Pantalla.LECTURA, Pantalla.CUESTIONARIO)
        ) {
            estado = estado.copy(regresarAlCuestionario = false)
            ir(if (estado.sesion?.rol == "DOCENTE") Pantalla.PANEL_DOCENTE else Pantalla.MISIONES)
        } else ir(Pantalla.INICIO)
    }

    fun cerrarSesion() {
        operacionActual++
        trabajo?.cancel()
        almacen.borrar()
        estado = EstadoApp(tamanoTexto = estado.tamanoTexto, movimiento = estado.movimiento)
    }
}
