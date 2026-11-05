package com.canadulce.app.datos

import org.json.JSONArray
import org.json.JSONObject

data class EdicionLectura(
    val titulo: String,
    val contenido: String,
    val grado: Int,
    val dificultad: String,
    val categoria: String,
) {
    fun aJson() = JSONObject()
        .put("titulo", titulo.trim())
        .put("contenido", contenido.trim())
        .put("grado", grado)
        .put("dificultad", dificultad)
        .put("categoria", categoria.trim().takeIf { it.isNotEmpty() } ?: JSONObject.NULL)
}

data class EdicionPregunta(
    val enunciado: String,
    val tipo: String,
    val orden: Int,
    val opciones: List<String>,
    val correcta: Int,
    val estado: String,
) {
    fun aJson(): JSONObject {
        val lista = JSONArray()
        opciones.forEachIndexed { indice, texto ->
            lista.put(
                JSONObject().put("texto", texto.trim())
                    .put("orden", indice + 1)
                    .put("esCorrecta", indice == correcta),
            )
        }
        return JSONObject().put("enunciado", enunciado.trim())
            .put("tipo", tipo).put("orden", orden).put("opciones", lista)
    }
}
