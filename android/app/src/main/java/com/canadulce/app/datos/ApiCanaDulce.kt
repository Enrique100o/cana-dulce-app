package com.canadulce.app.datos

import com.canadulce.app.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ErrorApi(val estado: Int, override val message: String) : Exception(message)

class ApiCanaDulce {
    suspend fun solicitar(
        ruta: String,
        metodo: String = "GET",
        datos: JSONObject? = null,
        token: String? = null,
    ): JSONObject =
        withContext(Dispatchers.IO) {
            val conexion =
                URL(BuildConfig.API_URL + ruta.removePrefix("/")).openConnection()
                    as HttpURLConnection
            try {
                conexion.requestMethod = metodo
                conexion.connectTimeout = 20_000
                conexion.readTimeout = 90_000
                conexion.setRequestProperty("Accept", "application/json")
                token?.let { conexion.setRequestProperty("Authorization", "Bearer $it") }
                if (datos != null) {
                    conexion.doOutput = true
                    conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    conexion.outputStream.use {
                        it.write(datos.toString().toByteArray(Charsets.UTF_8))
                    }
                }
                val estado = conexion.responseCode
                if (estado == HttpURLConnection.HTTP_NO_CONTENT) return@withContext JSONObject()
                val entrada = if (estado in 200..299) conexion.inputStream else conexion.errorStream
                val texto = entrada?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json =
                    try {
                        JSONObject(texto)
                    } catch (_: Exception) {
                        throw ErrorApi(
                            estado,
                            "Estamos preparando la conexión. Inténtalo otra vez en unos segundos.",
                        )
                    }
                if (estado !in 200..299) {
                    throw ErrorApi(
                        estado,
                        json.optString("mensaje", "No pudimos completar la solicitud."),
                    )
                }
                json
            } catch (error: ErrorApi) {
                throw error
            } catch (_: IOException) {
                throw ErrorApi(
                    0,
                    "No pudimos conectarnos. Revisa tu Internet y vuelve a intentarlo.",
                )
            } finally {
                conexion.disconnect()
            }
        }
}
