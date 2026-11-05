package com.canadulce.app

import android.content.pm.ApplicationInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canadulce.app.datos.AlmacenSesion
import com.canadulce.app.datos.Sesion
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntregaNubeTest {
    @get:Rule val interfaz = createEmptyComposeRule()

    @Test
    fun apkFirmadoConsultaLaNubeConLosDosRoles() {
        val args = InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("validarEntrega") == "si")
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(0, contexto.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        val almacen = AlmacenSesion(contexto)
        try {
            almacen.guardar(Sesion(checkNotNull(args.getString("sesionEstudiante")), "ESTUDIANTE", null))
            ActivityScenario.launch(MainActivity::class.java).use {
                esperar("¿Con quién quieres jugar?")
                pulsar("Elegir a Lina")
                esperar("Lina te acompaña.", parcial = true)
                pulsar("La luz del planeta Menta")
                esperar("Tamaño del texto")
            }
            almacen.guardar(Sesion(checkNotNull(args.getString("sesionDocente")), "DOCENTE", null))
            ActivityScenario.launch(MainActivity::class.java).use {
                esperar("Área docente")
                pulsar("Estudiantes")
                esperar("Buscar por alias")
                val alias = checkNotNull(args.getString("aliasDemostracion"))
                interfaz.onNodeWithText("Buscar por alias").performTextInput(alias)
                val ficha = hasText(alias) and hasText("Consultar progreso →") and isEnabled()
                interfaz.waitUntil(25_000) {
                    interfaz.onAllNodes(ficha).fetchSemanticsNodes(false).size == 1
                }
                interfaz.onNode(ficha).performScrollTo().performClick()
                esperar("Colección de insignias")
            }
        } finally {
            almacen.borrar()
        }
    }

    private fun esperar(texto: String, parcial: Boolean = false) {
        interfaz.waitUntil(45_000) {
            val textoBuscado = hasText(texto, substring = parcial)
            if (interfaz.onAllNodes(textoBuscado).fetchSemanticsNodes(false).isNotEmpty()) true
            else runCatching {
                interfaz.onNode(hasScrollToIndexAction()).performScrollToNode(textoBuscado)
                true
            }.getOrDefault(false)
        }
    }

    private fun pulsar(texto: String) {
        esperar(texto)
        val nodo = interfaz.onNodeWithText(texto)
        runCatching { nodo.performScrollTo() }
        nodo.performClick()
    }
}
