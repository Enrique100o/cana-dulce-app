package com.canadulce.app

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import com.canadulce.app.datos.ApiCanaDulce
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujosSprintDosTest {
    @get:Rule val interfaz = createEmptyComposeRule()
    private val instrumento = InstrumentationRegistry.getInstrumentation()
    private val api = ApiCanaDulce()
    private var actividad: ActivityScenario<MainActivity>? = null

    @Before
    fun iniciar() {
        assumeTrue(BuildConfig.API_URL.startsWith("http://10.0.2.2:"))
        val contexto = instrumento.targetContext
        File(contexto.noBackupFilesDir, "sesion.enc").delete()
        contexto.getSharedPreferences("lectura", 0).edit().putBoolean("movimiento", true).commit()
    }

    @After
    fun cerrar() {
        actividad?.close()
    }

    private fun lanzar() {
        actividad = ActivityScenario.launch(MainActivity::class.java)
    }

    private fun esperar(texto: String, parcial: Boolean = false) {
        interfaz.waitUntil(12_000) {
            if (interfaz.onAllNodesWithText(texto, substring = parcial).fetchSemanticsNodes(false).isNotEmpty()) {
                true
            } else runCatching {
                interfaz.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(texto, substring = parcial))
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

    private fun escribir(campo: String, texto: String) {
        closeSoftKeyboard()
        esperar(campo)
        val nodo = interfaz.onNodeWithText(campo)
        runCatching { nodo.performScrollTo() }
        interfaz.waitForIdle()
        interfaz.onNodeWithText(campo).performTextInput(texto)
        closeSoftKeyboard()
    }

    private fun capturar(nombre: String) {
        interfaz.waitForIdle()
        val carpeta = File(instrumento.targetContext.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        val imagen = checkNotNull(instrumento.uiAutomation.takeScreenshot())
        File(carpeta, "$nombre.png").outputStream().use { imagen.compress(Bitmap.CompressFormat.PNG, 100, it) }
        imagen.recycle()
    }

    private fun registrar(alias: String, grado: Int): Int = runBlocking {
        api.solicitar(
            "api/estudiantes", "POST",
            JSONObject().put("alias", alias).put("grado", grado).put("codigoAcceso", "PruebaAstro24"),
        ).getInt("idEstudiante")
    }

    @Test
    fun recuperaLaMisionYDesbloqueaInsigniasReales() {
        val alias = "Lina" + System.currentTimeMillis().toString().takeLast(7)
        registrar(alias, 3)
        lanzar()
        esperar("Lee y despega")
        capturar("Sprint2_01_Despegue")
        pulsar("Entrar como estudiante")
        escribir("Tu alias", alias)
        escribir("Código de acceso", "PruebaAstro24")
        pulsar("Entrar")
        esperar("¿Con quién quieres jugar?")
        capturar("Final_01_Tripulacion")
        pulsar("Elegir a Lina")
        esperar("Misiones disponibles:", parcial = true)
        pulsar("Entrenamiento")
        pulsar("Una brújula")
        esperar("Probemos otra vez")
        capturar("Final_02_Pista")
        pulsar("Una caja de semillas")
        esperar("¡Acertaste!")
        capturar("Final_03_Acierto")
        pulsar("‹ Mapa estelar")
        capturar("Sprint2_02_Mapa")
        pulsar("La luz del planeta Menta")
        pulsar("Ir al cuestionario")
        pulsar("El cable estaba suelto.")
        pulsar("Siguiente pregunta")
        esperar("¡Acertaste!")
        pulsar("Continuar")
        esperar("PREGUNTA 2 DE 3")
        actividad?.close()
        lanzar()
        esperar("Lina te acompaña.", parcial = true)
        pulsar("Retomar misión →")
        esperar("PREGUNTA 2 DE 3")
        esperar("1 de 3 confirmadas")
        capturar("Sprint2_03_Cuestionario")
        pulsar("Anterior")
        esperar("Tu respuesta: El cable estaba suelto.")
        pulsar("Continuar")
        pulsar("Apagaron el interruptor.")
        pulsar("Siguiente pregunta")
        esperar("¡Acertaste!")
        pulsar("Continuar")
        pulsar("La lámpara volvió a funcionar al ajustar el cable.")
        pulsar("Finalizar misión")
        esperar("¡Acertaste!")
        pulsar("Ver resultados")
        esperar("100,00 puntos")
        interfaz.onNodeWithContentDescription("3 estrellas").assertExists()
        esperar("¡Llegaste a la Luna!")
        capturar("Final_04_Llegada_lunar")
        esperar("Ver reacción de Lina")
        interfaz.onAllNodesWithText("Ver reacción de Lina").onFirst().performScrollTo().performClick()
        esperar("¡Bien hecho!")
        capturar("Final_05_Revision")
        capturar("Sprint2_04_Resultado")
        pulsar("Mi progreso")
        esperar("Colección de insignias")
        capturar("Sprint2_05_Bitacora")
        esperar("Lectura brillante")
        interfaz.onAllNodesWithText("Desbloqueada ✓").assertCountEquals(2)
        pulsar("La luz del planeta Menta")
        esperar("Revisa tus respuestas")
        esperar("Tu respuesta: El cable estaba suelto.")
    }

    @Test
    fun confirmaPreguntaIncorrectaAntesDeContinuar() {
        val alias = "Astro" + System.currentTimeMillis().toString().takeLast(7)
        registrar(alias, 3)
        lanzar()
        pulsar("Entrar como estudiante")
        escribir("Tu alias", alias)
        escribir("Código de acceso", "PruebaAstro24")
        pulsar("Entrar")
        pulsar("Elegir a Astro")
        pulsar("La luz del planeta Menta")
        pulsar("Ir al cuestionario")
        pulsar("Siguiente pregunta")
        esperar("Elige una respuesta antes de continuar.")
        pulsar("Entendido")
        pulsar("La batería estaba rota.")
        pulsar("Siguiente pregunta")
        esperar("Esta vez no acertamos")
        interfaz.onNodeWithContentDescription("Astro baja la cabeza", substring = true).assertIsDisplayed()
        capturar("Pregunta_01_Error")
        actividad?.close()
        lanzar()
        pulsar("Retomar misión →")
        esperar("Esta vez no acertamos")
        esperar("Tu respuesta: La batería estaba rota.")
        pulsar("Continuar")
        pulsar("Apagaron el interruptor.")
        pulsar("Siguiente pregunta")
        esperar("¡Acertaste!")
        interfaz.onNodeWithContentDescription("Astro salta de alegría", substring = true).assertIsDisplayed()
        capturar("Pregunta_02_Acierto")
        pulsar("Anterior")
        esperar("Esta vez no acertamos")
        pulsar("Continuar")
        esperar("¡Acertaste!")
        pulsar("Continuar")
        pulsar("La batería no cabía en el refugio.")
        pulsar("Finalizar misión")
        esperar("Esta vez no acertamos")
        capturar("Pregunta_03_Error")
        pulsar("Ver resultados")
        esperar("33,33 puntos")
        esperar("1 de 3 respuestas correctas")
        capturar("Pregunta_04_Resultado")
    }

    @Test
    fun docenteCreaEditaPublicaDesactivaYConsultaProgreso() {
        val sufijo = System.currentTimeMillis().toString().takeLast(7)
        val alias = "Mara$sufijo"
        registrar(alias, 4)
        val titulo = "Estación de prueba $sufijo"
        lanzar()
        pulsar("Acceso docente")
        escribir("Usuario docente", "docente")
        escribir("Contraseña", "PruebaDocente42")
        pulsar("Entrar al área docente")
        esperar("Biblioteca de lecturas")
        capturar("Docente_01_Biblioteca")
        pulsar("Nueva lectura")
        capturar("Docente_02_Editor_Lectura")
        escribir("Título", titulo)
        escribir("Texto de la lectura", "La tripulación guardó las semillas para el viaje a la Luna.")
        pulsar("Guardar lectura")
        pulsar("Publicar misión")
        esperar("La lectura no tiene un cuestionario válido para publicarse.")
        pulsar("Entendido")
        pulsar("Editar lectura")
        escribir("Categoría (opcional)", "Exploración")
        pulsar("Guardar lectura")
        pulsar("Agregar pregunta")
        capturar("Docente_03_Editor_Pregunta")
        escribir("Enunciado", "¿Qué guardó la tripulación?")
        escribir("Opción A", "Semillas")
        escribir("Opción B", "Rocas")
        esperar("Guardar pregunta")
        interfaz.onNodeWithContentDescription("Marcar opción A como correcta")
            .performScrollTo().performClick()
        pulsar("Guardar pregunta")
        pulsar("Editar pregunta")
        esperar("Opción B")
        interfaz.onNodeWithText("Opción B").performScrollTo().performTextClearance()
        escribir("Opción B", "Rocas lunares")
        pulsar("Guardar pregunta")
        esperar("Rocas lunares", parcial = true)
        pulsar("Publicar misión")
        esperar("Misión publicada.")
        capturar("Sprint2_06_Gestion_docente")
        capturar("Docente_04_Gestion")
        pulsar("Desactivar pregunta")
        pulsar("Confirmar desactivación")
        esperar("Una lectura publicada debe conservar al menos una pregunta activa válida.")
        esperar("Entendido")
        pulsar("Entendido")
        pulsar("Desactivar lectura")
        pulsar("Confirmar desactivación")
        esperar("Lectura desactivada.")
        pulsar("Desactivar pregunta")
        pulsar("Confirmar desactivación")
        esperar("Pregunta desactivada.")
        pulsar("‹ Área docente")
        pulsar("Estudiantes")
        esperar("Seguimiento del aula")
        capturar("Docente_05_Estudiantes")
        escribir("Buscar por alias", alias)
        pulsar("Grado: todos ▾")
        pulsar("4")
        pulsar("Estado: todos ▾")
        pulsar("ACTIVO")
        val ficha = hasText(alias) and hasText("Consultar progreso →") and isEnabled()
        interfaz.waitUntil(12_000) {
            interfaz.onAllNodes(ficha).fetchSemanticsNodes(false).size == 1
        }
        interfaz.onNode(ficha).performScrollTo().performClick()
        esperar("4.º grado · Cadete espacial")
        capturar("Docente_06_Progreso")
        pulsar("‹ Estudiantes")
        pulsar("Lecturas")
        escribir("Buscar una lectura", titulo)
        val fichaLectura = hasText(titulo) and hasText("Administrar lectura →") and isEnabled()
        interfaz.waitUntil(12_000) {
            interfaz.onAllNodes(fichaLectura).fetchSemanticsNodes(false).size == 1
        }
        interfaz.onNode(fichaLectura).performScrollTo().performClick()
        pulsar("Eliminar lectura")
        esperar("¿Eliminar esta lectura?")
        capturar("Docente_07_Confirmacion")
        pulsar("Cancelar")
        esperar("Prepara tu misión")
        pulsar("Eliminar lectura")
        pulsar("Confirmar eliminación")
        esperar("Lectura eliminada.")
        esperar("No hay lecturas que coincidan con la búsqueda.")
        capturar("Docente_08_Eliminada")
    }
}
