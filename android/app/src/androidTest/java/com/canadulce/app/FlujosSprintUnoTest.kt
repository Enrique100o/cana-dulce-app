package com.canadulce.app

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujosSprintUnoTest {
    @get:Rule val interfaz = createEmptyComposeRule()
    private lateinit var actividad: ActivityScenario<MainActivity>
    private val argumentos
        get() = InstrumentationRegistry.getArguments()

    private val contexto
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun iniciar() {
        File(contexto.noBackupFilesDir, "sesion.enc").delete()
        contexto.getSharedPreferences("lectura", 0).edit().putInt("tamano", 20).commit()
        actividad = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun cerrar() {
        actividad.close()
    }

    private fun esperar(texto: String, parcial: Boolean = false) {
        interfaz.waitUntil(120_000) {
            val visible = interfaz
                .onAllNodesWithText(texto, substring = parcial)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
            if (visible) true
            else runCatching {
                interfaz.onNode(hasScrollToIndexAction())
                    .performScrollToNode(hasText(texto, substring = parcial))
                true
            }.getOrDefault(false)
        }
    }

    private fun pulsar(texto: String) {
        esperar(texto)
        val nodo = interfaz.onNodeWithText(texto)
        try {
            nodo.performScrollTo()
        } catch (_: AssertionError) {}
        nodo.performClick()
    }

    private fun escribir(campo: String, valor: String) {
        esperar(campo)
        val nodo = interfaz.onNodeWithText(campo)
        try {
            nodo.performScrollTo()
        } catch (_: AssertionError) {}
        nodo.performTextInput(valor)
    }

    private fun captura(nombre: String) {
        interfaz.waitForIdle()
        val carpeta = File(contexto.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        val imagen = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        checkNotNull(imagen).let { bitmap ->
            File(carpeta, nombre + ".png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    private fun ejecutarShell(comando: String): String {
        val descriptor =
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(comando)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use {
            it.readText().trim()
        }
    }

    @Test
    fun mensajeCuandoNoHayInternet() {
        val wifi = ejecutarShell("settings get global wifi_on") == "1"
        val datos = ejecutarShell("settings get global mobile_data") == "1"
        try {
            ejecutarShell("svc wifi disable")
            ejecutarShell("svc data disable")
            esperar("Entrar como estudiante")
            pulsar("Entrar como estudiante")
            escribir("Tu alias", "ModoSinRed")
            escribir("Código de acceso", "CodigoRed123")
            pulsar("Entrar")
            esperar("No pudimos conectarnos. Revisa tu Internet y vuelve a intentarlo.")
            captura("11_Error_conexion")
            interfaz.onNodeWithText("Mi aventura").assertDoesNotExist()
        } finally {
            ejecutarShell("svc wifi " + if (wifi) "enable" else "disable")
            ejecutarShell("svc data " + if (datos) "enable" else "disable")
        }
    }

    @Test
    fun registroSesionesMisionesYLectura() {
        val aliasNuevo = checkNotNull(argumentos.getString("aliasNuevo"))
        val claveNueva = checkNotNull(argumentos.getString("claveNueva"))
        val usuarioDocente = argumentos.getString("usuarioDocente") ?: "docente"
        val claveDocente = checkNotNull(argumentos.getString("claveDocente"))
        val tituloLectura = argumentos.getString("tituloLectura") ?: "La semilla de Sofía"

        esperar("Entrar como estudiante")
        captura("01_Inicio")
        pulsar("Crear mi perfil")
        pulsar("Crear mi perfil")
        esperar("Tu alias debe tener entre 3 y 30 caracteres.")
        pulsar("Entendido")
        escribir("Tu alias", aliasNuevo)
        pulsar("Tercero")
        escribir("Código de acceso", claveNueva)
        pulsar("Crear mi perfil")
        esperar("¿Con quién quieres jugar?")
        pulsar("Elegir a Astro")
        esperar("Mi aventura")
        captura("02_Perfil_creado")
        pulsar("Cerrar sesión")
        esperar("Entrar como estudiante")

        pulsar("Crear mi perfil")
        escribir("Tu alias", aliasNuevo)
        pulsar("Tercero")
        escribir("Código de acceso", claveNueva)
        pulsar("Crear mi perfil")
        esperar("Ese alias ya está en uso. Elige otro.")
        captura("03_Alias_repetido")
        pulsar("‹ Volver")
        esperar("Entrar como estudiante")
        pulsar("Entrar como estudiante")
        escribir("Tu alias", aliasNuevo)
        escribir("Código de acceso", "CodigoIncorrecto")
        pulsar("Entrar")
        esperar("Alias o código de acceso incorrectos.")
        pulsar("Entendido")
        interfaz.onNodeWithText("Código de acceso").performTextClearance()
        escribir("Código de acceso", claveNueva)
        pulsar("Entrar")
        esperar(tituloLectura)
        interfaz.onNodeWithText("Dificultad: Baja · 1 pregunta").assertExists()
        captura("04_Misiones")

        escribir("Buscar una misión", "SEMILLA")
        esperar(tituloLectura)
        pulsar("Limpiar")

        escribir("Buscar una misión", "TextoQueNoExiste")
        esperar("No encontramos ese título.", parcial = true)
        pulsar("Limpiar")
        esperar(tituloLectura)
        interfaz.onNodeWithText(tituloLectura).performClick()
        esperar("Tamaño del texto")
        interfaz.onNodeWithContentDescription("Aumentar texto").performClick()
        interfaz.onNodeWithContentDescription("Aumentar texto").performClick()
        interfaz.onNodeWithContentDescription("Aumentar texto").performClick()
        interfaz.onNodeWithContentDescription("Tamaño actual 26").assertExists()
        captura("05_Lectura_ajustable")
        actividad.recreate()
        esperar("Tamaño del texto")
        interfaz.onNodeWithContentDescription("Tamaño actual 26").assertExists()

        actividad.close()
        actividad = ActivityScenario.launch(MainActivity::class.java)
        esperar(tituloLectura)
        interfaz.onNodeWithText(tituloLectura).performClick()
        esperar("Tamaño del texto")
        interfaz.onNodeWithContentDescription("Tamaño actual 26").assertExists()
        pulsar("Ir al cuestionario")
        esperar("¿Qué sembró Sofía junto a su casa?")
        pulsar("Finalizar misión")
        esperar("Elige una respuesta antes de continuar.")
        pulsar("Entendido")
        pulsar("Una semilla")
        captura("09_Cuestionario")
        pulsar("Releer historia")
        esperar("Tamaño del texto")
        interfaz.onNodeWithContentDescription("Tamaño actual 26").assertExists()
        pulsar("Volver al cuestionario")
        esperar("¿Qué sembró Sofía junto a su casa?")
        interfaz.onNodeWithText("Una semilla").assertIsSelected()
        actividad.recreate()
        esperar("¿Qué sembró Sofía junto a su casa?")
        interfaz.onNodeWithText("Una semilla").assertIsSelected()
        pulsar("Finalizar misión")
        esperar("¡Acertaste!")
        pulsar("Ver resultados")
        esperar("¡Misión completada!")
        interfaz.onNodeWithContentDescription("3 estrellas").assertExists()
        captura("10_Resultado")
        pulsar("Volver a misiones")
        pulsar("Cerrar sesión")
        actividad.close()
        actividad = ActivityScenario.launch(MainActivity::class.java)
        esperar("Entrar como estudiante")

        pulsar("Acceso docente")
        escribir("Usuario docente", usuarioDocente)
        escribir("Contraseña", "Incorrecta123")
        pulsar("Entrar al área docente")
        esperar("Usuario o contraseña incorrectos.")
        pulsar("Entendido")
        interfaz.onNodeWithText("Contraseña").performTextClearance()
        escribir("Contraseña", claveDocente)
        pulsar("Entrar al área docente")
        esperar("Área docente")
        captura("06_Area_docente")
        pulsar("Estudiantes")
        esperar(aliasNuevo)
        captura("07_Estudiantes")
        pulsar("Cerrar sesión")
        esperar("Entrar como estudiante")
    }
}
