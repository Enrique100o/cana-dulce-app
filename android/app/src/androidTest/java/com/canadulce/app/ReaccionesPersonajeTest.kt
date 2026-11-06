package com.canadulce.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.canadulce.app.datos.Companero
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.RespuestaEvaluada
import com.canadulce.app.datos.Resultado
import com.canadulce.app.datos.Sesion
import com.canadulce.app.ui.componentes.GestoOrbital
import com.canadulce.app.ui.componentes.PersonajeOrbital
import com.canadulce.app.ui.pantallas.PantallaEntrenamiento
import com.canadulce.app.ui.pantallas.PantallaResultado
import com.canadulce.app.ui.theme.CanaDulceTheme
import java.io.File
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaccionesPersonajeTest {
    @get:Rule val interfaz = createEmptyComposeRule()
    private var actividad: ActivityScenario<MainActivity>? = null

    @After
    fun cerrar() {
        actividad?.close()
    }

    private fun mostrar(contenido: @Composable () -> Unit) {
        actividad = ActivityScenario.launch(MainActivity::class.java)
        actividad?.onActivity { activity ->
            activity.setContent {
                CanaDulceTheme(darkTheme = true) { Surface(content = contenido) }
            }
        }
    }

    private fun capturar(personaje: String, gesto: String): Bitmap {
        interfaz.waitForIdle()
        val bitmap = interfaz.onNodeWithContentDescription(personaje, substring = true)
            .captureToImage().asAndroidBitmap()
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val carpeta = File(contexto.getExternalFilesDir(null), "reacciones").apply { mkdirs() }
        File(carpeta, "${personaje}_$gesto.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return bitmap
    }

    private fun capturarPantalla(nombre: String) {
        interfaz.waitForIdle()
        val instrumento = InstrumentationRegistry.getInstrumentation()
        val carpeta = File(instrumento.targetContext.getExternalFilesDir(null), "reacciones")
        carpeta.mkdirs()
        val bitmap = checkNotNull(instrumento.uiAutomation.takeScreenshot())
        File(carpeta, "$nombre.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }

    private fun comprobarMovimiento(companero: Companero) {
        val gesto = mutableStateOf(GestoOrbital.CELEBRAR)
        val movimiento = mutableStateOf(true)
        val repeticion = mutableIntStateOf(0)
        interfaz.mainClock.autoAdvance = false
        mostrar {
            Box {
                PersonajeOrbital(
                    companero, movimiento.value, Modifier.size(220.dp, 244.dp),
                    gesto.value, repeticion.intValue,
                )
            }
        }
        interfaz.mainClock.advanceTimeByFrame()
        val inicio = capturar(companero.nombre, "inicio")
        interfaz.mainClock.advanceTimeBy(400)
        val salto = capturar(companero.nombre, "salto")
        assertFalse("El acierto debe mover al personaje", inicio.sameAs(salto))
        interfaz.mainClock.advanceTimeBy(1800)
        val final = capturar(companero.nombre, "celebracion")
        interfaz.runOnIdle { repeticion.intValue++ }
        interfaz.mainClock.advanceTimeBy(432)
        val repetida = capturar(companero.nombre, "repetida")
        assertFalse("La repetición debe iniciar un salto nuevo", final.sameAs(repetida))

        interfaz.runOnIdle { gesto.value = GestoOrbital.DESANIMAR }
        interfaz.mainClock.advanceTimeByFrame()
        interfaz.mainClock.advanceTimeByFrame()
        val inicioError = capturar(companero.nombre, "inicio_error")
        interfaz.mainClock.advanceTimeBy(560)
        val error = capturar(companero.nombre, "desanimo")
        assertFalse("El error debe bajar la cabeza y los brazos", inicioError.sameAs(error))

        interfaz.runOnIdle { movimiento.value = false }
        interfaz.mainClock.advanceTimeBy(48)
        val pausa = capturar(companero.nombre, "pausa")
        interfaz.mainClock.advanceTimeBy(2000)
        val sigueEnPausa = capturar(companero.nombre, "pausa_final")
        assertTrue("Pausar debe conservar el dibujo quieto", pausa.sameAs(sigueEnPausa))
        interfaz.onNodeWithContentDescription("${companero.nombre} baja la cabeza y los brazos", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun astroSaltaSeDesanimaYRespetaLaPausa() = comprobarMovimiento(Companero.ASTRO)

    @Test
    fun linaSaltaSeDesanimaYRespetaLaPausa() = comprobarMovimiento(Companero.LINA)

    @Test
    fun entrenamientoMuestraLaReaccionYPermiteRepetirla() {
        mostrar { PantallaEntrenamiento(EstadoApp(companero = Companero.ASTRO)) {} }
        interfaz.onNodeWithText("Una brújula").performScrollTo().performClick()
        interfaz.onNodeWithText("Probemos otra vez").assertIsDisplayed()
        interfaz.onNodeWithContentDescription("Astro baja la cabeza", substring = true).assertIsDisplayed()
        capturarPantalla("Entrenamiento_error")
        interfaz.onNodeWithText("Repetir animación").performClick()
        interfaz.onNodeWithText("Una caja de semillas").performScrollTo().performClick()
        interfaz.onNodeWithText("¡Acertaste!").assertIsDisplayed()
        interfaz.onNodeWithContentDescription("Astro salta de alegría", substring = true).assertIsDisplayed()
        capturarPantalla("Entrenamiento_acierto")
        interfaz.onNodeWithText("Repetir animación").performClick()
        interfaz.onNodeWithText("Siguiente reto").performScrollTo().performClick()
        capturarPantalla("Entrenamiento_siguiente")
        interfaz.waitUntil(5000) {
            interfaz.onNodeWithText("PRÁCTICA 2 DE 3").isDisplayed()
        }
    }

    @Test
    fun resultadoMuestraReaccionesParaLosAciertosYErrores() {
        val estado = EstadoApp(
            companero = Companero.LINA,
            sesion = Sesion("sesion-de-prueba", "ESTUDIANTE", null),
            resultado = Resultado(
                50.0, 1, 1, 2,
                listOf(
                    RespuestaEvaluada("¿Qué llevó Lina?", "Semillas", true),
                    RespuestaEvaluada("¿Qué buscaron?", "Agua", false),
                ),
            ),
        )
        mostrar { PantallaResultado(estado, {}, {}) }
        interfaz.onAllNodesWithText("Ver reacción de Lina")[0].performScrollTo().performClick()
        interfaz.onNodeWithText("¡Bien hecho!").assertIsDisplayed()
        interfaz.onNodeWithContentDescription("Lina salta de alegría", substring = true)
            .performScrollTo().assertIsDisplayed()
        interfaz.onNodeWithText("Repetir animación").performScrollTo().performClick()
        interfaz.onNodeWithText("Cerrar reacción").performScrollTo().performClick()
        interfaz.onAllNodesWithText("Ver reacción de Lina")[1].performScrollTo().performClick()
        interfaz.onNodeWithText("Volvamos a la historia").assertIsDisplayed()
        interfaz.onNodeWithContentDescription("Lina baja la cabeza", substring = true)
            .performScrollTo().assertIsDisplayed()
        capturarPantalla("Revision_error_Lina")
    }
}
