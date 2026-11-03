package com.canadulce.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.canadulce.app.datos.*
import java.net.URLEncoder
import java.text.Normalizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class PrincipalViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val api = ApiCanaDulce()
    private val almacen = AlmacenSesion(aplicacion)
    private val preferencias = aplicacion.getSharedPreferences("lectura", 0)
    private var trabajo: Job? = null
    private var operacionActual = 0
    var estado by
        mutableStateOf(EstadoApp(tamanoTexto = preferencias.getInt("tamano", 20).coerceIn(18, 32)))
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
                if (fallo.estado in listOf(401, 403) && estado.sesion != null) {
                    val docente = estado.sesion?.rol == "DOCENTE"
                    almacen.borrar()
                    estado =
                        EstadoApp(
                            pantalla = if (docente) Pantalla.DOCENTE else Pantalla.ESTUDIANTE,
                            error = "Tu sesión terminó. Vuelve a ingresar.",
                            tamanoTexto = estado.tamanoTexto,
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
        cargarDatos("")
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
        cargarDatos(estado.busqueda)
    }

    private suspend fun cargarDatos(busqueda: String) {
        val sesion = estado.sesion ?: return
        val filtro =
            busqueda
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.let { "?q=" + URLEncoder.encode(it, "UTF-8") }
                .orEmpty()
        val json = api.solicitar("api/lecturas$filtro", token = sesion.token)
        val datos = json.getJSONArray("datos")
        val lecturas = List(datos.length()) { Lectura.desdeJson(datos.getJSONObject(it)) }
        var estudiantes = estado.estudiantes
        if (sesion.rol == "DOCENTE") {
            val lista = api.solicitar("api/estudiantes", token = sesion.token).getJSONArray("datos")
            estudiantes = List(lista.length()) { Perfil.desdeJson(lista.getJSONObject(it)) }
        }
        estado = estado.copy(lecturas = lecturas, estudiantes = estudiantes)
    }

    fun buscar(texto: String) {
        if (longitud(texto) > 100) return
        estado = estado.copy(busqueda = texto)
        ejecutar {
            delay(350)
            cargarDatos(texto)
        }
    }

    fun refrescar() = ejecutar { cargarDatos(estado.busqueda) }

    fun abrirLectura(id: Int) = ejecutar {
        val sesion = estado.sesion ?: return@ejecutar
        val lectura = Lectura.desdeJson(api.solicitar("api/lecturas/$id", token = sesion.token))
        estado = estado.copy(lectura = lectura, pantalla = Pantalla.LECTURA)
    }

    fun cambiarTexto(cambio: Int) {
        val tamano = (estado.tamanoTexto + cambio).coerceIn(18, 32)
        estado = estado.copy(tamanoTexto = tamano)
        preferencias.edit().putInt("tamano", tamano).apply()
    }

    fun volver() {
        if (estado.pantalla == Pantalla.LECTURA) {
            ir(if (estado.sesion?.rol == "DOCENTE") Pantalla.PANEL_DOCENTE else Pantalla.MISIONES)
        } else ir(Pantalla.INICIO)
    }

    fun cerrarSesion() {
        operacionActual++
        trabajo?.cancel()
        almacen.borrar()
        estado = EstadoApp(tamanoTexto = estado.tamanoTexto)
    }
}
