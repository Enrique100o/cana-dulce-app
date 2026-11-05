package com.canadulce.app.datos

import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

internal fun <T> JSONArray.leer(transformar: (JSONObject) -> T): List<T> =
    List(length()) { transformar(getJSONObject(it)) }

fun puntos(valor: Double): String =
    String.format(Locale.forLanguageTag("es-CO"), "%.2f", valor)

data class Insignia(val codigo: String, val nombre: String, val obtenida: Boolean)

data class Progreso(
    val alias: String,
    val grado: Int,
    val misiones: Int,
    val completados: Int,
    val puntos: Double,
    val estrellas: Int,
    val promedio: Double?,
    val mejor: Double?,
    val insignias: List<Insignia>,
) {
    companion object {
        fun desdeJson(json: JSONObject) =
            Progreso(
                json.getString("alias"),
                json.getInt("grado"),
                json.getInt("lecturasCompletadas"),
                json.getInt("intentosCompletados"),
                json.getDouble("puntosAcumulados"),
                json.getInt("estrellasAcumuladas"),
                json.optDouble("puntajePromedio").takeIf(Double::isFinite),
                json.optDouble("mejorPuntaje").takeIf(Double::isFinite),
                json.getJSONArray("insignias").leer {
                    Insignia(it.getString("codigo"), it.getString("nombre"), it.getBoolean("obtenida"))
                },
            )
    }
}

data class Intento(
    val id: Int,
    val lectura: Int,
    val titulo: String,
    val estado: String,
    val puntaje: Double?,
    val estrellas: Int?,
) {
    companion object {
        fun desdeJson(json: JSONObject) =
            Intento(
                json.getInt("idIntento"),
                json.getInt("idLectura"),
                json.getString("tituloLectura"),
                json.getString("estado"),
                json.optDouble("puntaje").takeIf(Double::isFinite),
                json.optInt("estrellas", -1).takeIf { it >= 0 },
            )
    }
}

data class RespuestaEvaluada(val enunciado: String, val elegida: String, val correcta: Boolean)

data class Resultado(
    val puntaje: Double,
    val estrellas: Int,
    val correctas: Int,
    val total: Int,
    val respuestas: List<RespuestaEvaluada>,
) {
    companion object {
        fun desdeJson(json: JSONObject) =
            Resultado(
                json.getDouble("puntaje"),
                json.getInt("estrellas"),
                json.getInt("cantidadCorrectas"),
                json.getInt("totalPreguntas"),
                json.getJSONArray("respuestas").leer {
                    RespuestaEvaluada(
                        it.getString("enunciado"),
                        it.getString("textoOpcionElegida"),
                        it.getBoolean("esCorrecta"),
                    )
                },
            )
    }
}

data class GestionDocente(
    val listaEstudiantes: Boolean = false,
    val busqueda: String = "",
    val grado: Int? = null,
    val estadoPerfil: String? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val edicionPregunta: Pregunta? = null,
    val estudiante: Perfil? = null,
)
