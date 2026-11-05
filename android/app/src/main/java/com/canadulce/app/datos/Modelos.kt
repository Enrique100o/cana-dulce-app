package com.canadulce.app.datos

import org.json.JSONObject

data class Perfil(val idEstudiante: Int, val alias: String, val grado: Int, val estado: String = "ACTIVO") {
    fun aJson() =
        JSONObject().put("idEstudiante", idEstudiante).put("alias", alias).put("grado", grado)

    companion object {
        fun desdeJson(json: JSONObject) =
            Perfil(
                json.getInt("idEstudiante"),
                json.getString("alias"),
                json.getInt("grado"),
                json.optString("estado", "ACTIVO"),
            )
    }
}
data class Sesion(val token: String, val rol: String, val estudiante: Perfil?) {
    fun aJson() =
        JSONObject()
            .put("token", token)
            .put("rol", rol)
            .put("estudiante", estudiante?.aJson() ?: JSONObject.NULL)

    companion object {
        fun desdeJson(json: JSONObject) =
            Sesion(
                json.getString("token"),
                json.getString("rol"),
                json.optJSONObject("estudiante")?.let(Perfil::desdeJson),
            )
    }
}

data class Lectura(
    val id: Int,
    val titulo: String,
    val grado: Int,
    val dificultad: String,
    val categoria: String,
    val estado: String,
    val contenido: String = "",
    val totalPreguntas: Int = 0,
    val bloqueada: Boolean = false,
) {
    companion object {
        fun desdeJson(json: JSONObject) =
            Lectura(
                json.getInt("idLectura"),
                json.getString("titulo"),
                json.getInt("grado"),
                json.getString("dificultad"),
                if (json.isNull("categoria")) "" else json.getString("categoria"),
                json.getString("estado"),
                json.optString("contenido", ""),
                json.optInt("totalPreguntasActivas", 0),
                json.optBoolean("bloqueadaParaEdicion"),
            )
    }
}

enum class Pantalla {
    INICIO,
    REGISTRO,
    ESTUDIANTE,
    DOCENTE,
    MISIONES,
    COMPANERO,
    ENTRENAMIENTO,
    LECTURA,
    CUESTIONARIO,
    RESULTADO,
    PROGRESO,
    PANEL_DOCENTE,
    GESTION_LECTURA,
    EDITOR_LECTURA,
    EDITOR_PREGUNTA,
    RECUPERAR,
}

data class EstadoApp(
    val pantalla: Pantalla = Pantalla.INICIO,
    val sesion: Sesion? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
    val lecturas: List<Lectura> = emptyList(),
    val estudiantes: List<Perfil> = emptyList(),
    val lectura: Lectura? = null,
    val busqueda: String = "",
    val aliasSugerido: String = "",
    val tamanoTexto: Int = 20,
    val preguntas: List<Pregunta> = emptyList(),
    val selecciones: Map<Int, Int> = emptyMap(),
    val idIntento: Int? = null,
    val resultado: Resultado? = null,
    val regresarAlCuestionario: Boolean = false,
    val movimiento: Boolean = true,
    val companero: Companero? = null,
    val pasoPregunta: Int = 0,
    val pendientes: List<Intento> = emptyList(),
    val progreso: Progreso? = null,
    val historial: List<Intento> = emptyList(),
    val regresoResultado: Pantalla = Pantalla.MISIONES,
    val premiosNuevos: List<Insignia> = emptyList(),
    val gestion: GestionDocente = GestionDocente(),
)

data class Opcion(val id: Int, val texto: String, val correcta: Boolean = false)

data class Pregunta(
    val id: Int,
    val enunciado: String,
    val opciones: List<Opcion>,
    val tipo: String,
    val orden: Int,
    val estado: String,
) {
    companion object {
        fun desdeJson(json: JSONObject): Pregunta {
            val opciones = json.getJSONArray("opciones")
            return Pregunta(
                json.getInt("idPregunta"),
                json.getString("enunciado"),
                List(opciones.length()) {
                    val opcion = opciones.getJSONObject(it)
                    Opcion(
                        opcion.getInt("idOpcion"),
                        opcion.getString("texto"),
                        opcion.optBoolean("esCorrecta"),
                    )
                },
                json.getString("tipo"),
                json.getInt("orden"),
                json.optString("estado", "ACTIVA"),
            )
        }
    }
}
