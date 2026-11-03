package com.canadulce.app.datos

import org.json.JSONObject

data class Perfil(val idEstudiante: Int, val alias: String, val grado: Int) {
    fun aJson() =
        JSONObject().put("idEstudiante", idEstudiante).put("alias", alias).put("grado", grado)

    companion object {
        fun desdeJson(json: JSONObject) =
            Perfil(
                json.getInt("idEstudiante"),
                json.getString("alias"),
                json.getInt("grado"),
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
) {
    companion object {
        fun desdeJson(json: JSONObject) =
            Lectura(
                json.getInt("idLectura"),
                json.getString("titulo"),
                json.getInt("grado"),
                json.getString("dificultad"),
                if (json.isNull("categoria")) "Historias" else json.getString("categoria"),
                json.getString("estado"),
                json.optString("contenido", ""),
            )
    }
}

enum class Pantalla {
    INICIO,
    REGISTRO,
    ESTUDIANTE,
    DOCENTE,
    MISIONES,
    LECTURA,
    PANEL_DOCENTE,
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
)
