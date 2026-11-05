package com.canadulce.app.datos

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject

internal class BorradorCuestionario(contexto: Context) {
    private val preferencias = contexto.getSharedPreferences("cuestionarios", Context.MODE_PRIVATE)

    private fun clave(estudiante: Int, intento: Int) = "$estudiante/$intento"

    fun guardar(estudiante: Int, intento: Int, paso: Int, selecciones: Map<Int, Int>) {
        val datos = JSONObject()
        selecciones.forEach { (pregunta, opcion) -> datos.put(pregunta.toString(), opcion) }
        preferencias.edit { putString(clave(estudiante, intento), JSONObject().put("paso", paso)
            .put("opciones", datos).toString()) }
    }

    fun leer(estudiante: Int, intento: Int, preguntas: List<Pregunta>): Pair<Int, Map<Int, Int>> {
        val texto = preferencias.getString(clave(estudiante, intento), null) ?: return 0 to emptyMap()
        val datos = runCatching { JSONObject(texto) }.getOrNull() ?: return 0 to emptyMap()
        val opciones = datos.optJSONObject("opciones") ?: return 0 to emptyMap()
        val validas = buildMap {
            preguntas.forEach { pregunta ->
                val opcion = opciones.optInt(pregunta.id.toString(), -1)
                if (pregunta.opciones.any { it.id == opcion }) put(pregunta.id, opcion)
            }
        }
        return datos.optInt("paso").coerceIn(preguntas.indices) to validas
    }

    fun borrar(estudiante: Int, intento: Int) {
        preferencias.edit { remove(clave(estudiante, intento)) }
    }
}
