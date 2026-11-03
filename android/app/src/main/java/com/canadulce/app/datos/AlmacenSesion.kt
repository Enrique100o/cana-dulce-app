package com.canadulce.app.datos

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONObject

class AlmacenSesion(contexto: Context) {
    private val archivo = AtomicFile(File(contexto.noBackupFilesDir, "sesion.enc"))
    private val nombreClave = "cana_dulce_sesion"

    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (almacen.getKey(nombreClave, null) as? SecretKey)?.let {
            return it
        }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                        nombreClave,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }

    fun guardar(sesion: Sesion) {
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, clave())
        val contenido = cifrador.doFinal(sesion.aJson().toString().toByteArray(Charsets.UTF_8))
        val json =
            JSONObject()
                .put("iv", Base64.encodeToString(cifrador.iv, Base64.NO_WRAP))
                .put("contenido", Base64.encodeToString(contenido, Base64.NO_WRAP))
        val salida = archivo.startWrite()
        try {
            salida.write(json.toString().toByteArray(Charsets.UTF_8))
            archivo.finishWrite(salida)
        } catch (error: Exception) {
            archivo.failWrite(salida)
            throw error
        }
    }

    fun leer(): Sesion? =
        try {
            val json = JSONObject(archivo.openRead().bufferedReader().use { it.readText() })
            val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
            cifrador.init(
                Cipher.DECRYPT_MODE,
                clave(),
                GCMParameterSpec(128, Base64.decode(json.getString("iv"), Base64.NO_WRAP)),
            )
            val texto = cifrador.doFinal(Base64.decode(json.getString("contenido"), Base64.NO_WRAP))
            Sesion.desdeJson(JSONObject(texto.toString(Charsets.UTF_8)))
        } catch (_: Exception) {
            archivo.delete()
            null
        }

    fun borrar() = archivo.delete()
}
