package com.canadulce.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Pantalla
import com.canadulce.app.ui.componentes.*
import com.canadulce.app.ui.pantallas.*

@Composable
fun AplicacionCanaDulce(modelo: PrincipalViewModel) {
    val estado = modelo.estado
    BackHandler(
        estado.pantalla in
            listOf(
                Pantalla.REGISTRO,
                Pantalla.ESTUDIANTE,
                Pantalla.DOCENTE,
                Pantalla.LECTURA,
                Pantalla.CUESTIONARIO,
                Pantalla.RESULTADO,
                Pantalla.PROGRESO,
                Pantalla.GESTION_LECTURA,
                Pantalla.EDITOR_LECTURA,
                Pantalla.EDITOR_PREGUNTA,
                Pantalla.COMPANERO,
                Pantalla.ENTRENAMIENTO,
            ),
    ) {
        if (!estado.cargando) modelo.volver()
    }
    Scaffold { espacioSistema ->
        Box(Modifier.fillMaxSize().padding(espacioSistema).consumeWindowInsets(espacioSistema)) {
            when (estado.pantalla) {
                Pantalla.INICIO ->
                    PantallaInicio(
                        estado.movimiento,
                        modelo::alternarMovimiento,
                        { modelo.ir(Pantalla.ESTUDIANTE) },
                        { modelo.ir(Pantalla.REGISTRO) },
                        { modelo.ir(Pantalla.DOCENTE) },
                    )
                Pantalla.REGISTRO ->
                    PantallaRegistro(
                        estado,
                        modelo::volver,
                        modelo::registrar,
                        { modelo.ir(Pantalla.ESTUDIANTE) },
                        modelo::quitarMensaje,
                    )
                Pantalla.ESTUDIANTE,
                Pantalla.DOCENTE ->
                    key(estado.pantalla) {
                        PantallaAcceso(
                            estado,
                            estado.pantalla == Pantalla.DOCENTE,
                            modelo::volver,
                            if (estado.pantalla == Pantalla.DOCENTE) modelo::ingresarDocente
                            else modelo::ingresarEstudiante,
                            { modelo.ir(Pantalla.REGISTRO) },
                            modelo::quitarMensaje,
                        )
                    }
                Pantalla.MISIONES ->
                    PantallaMisiones(
                        estado,
                        modelo::buscar,
                        modelo::abrirLectura,
                        modelo::retomarIntento,
                        { modelo.abrirProgreso() },
                        modelo::alternarMovimiento,
                        modelo::refrescar,
                        modelo::cerrarSesion,
                        modelo::quitarMensaje,
                        { modelo.ir(Pantalla.COMPANERO) },
                        { modelo.ir(Pantalla.ENTRENAMIENTO) },
                    )
                Pantalla.COMPANERO ->
                    PantallaCompanero(estado, modelo::elegirCompanero, modelo::volver)
                Pantalla.ENTRENAMIENTO ->
                    PantallaEntrenamiento(estado, modelo::volver)
                Pantalla.PANEL_DOCENTE ->
                    PantallaDocente(estado, modelo)
                Pantalla.GESTION_LECTURA -> PantallaGestionLectura(estado, modelo)
                Pantalla.EDITOR_LECTURA -> PantallaEditarLectura(estado, modelo)
                Pantalla.EDITOR_PREGUNTA -> PantallaEditarPregunta(estado, modelo)
                Pantalla.PROGRESO ->
                    PantallaProgreso(
                        estado, modelo::volver, modelo::refrescarProgreso,
                        modelo::verResultado, modelo::quitarMensaje,
                    )
                Pantalla.LECTURA ->
                    PantallaLectura(
                        estado,
                        modelo::volver,
                        modelo::cambiarTexto,
                        modelo::abrirCuestionario,
                        modelo::quitarMensaje,
                    )
                Pantalla.CUESTIONARIO ->
                    PantallaCuestionario(
                        estado,
                        modelo::volver,
                        modelo::releer,
                        modelo::elegirRespuesta,
                        modelo::irAPregunta,
                        modelo::confirmarRespuestaActual,
                        modelo::continuarCuestionario,
                        modelo::quitarMensaje,
                    )
                Pantalla.RESULTADO ->
                    PantallaResultado(estado, modelo::volver) { modelo.abrirProgreso() }
                Pantalla.RECUPERAR ->
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LibroAventurero()
                        Text("Preparando tu aventura", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(20.dp))
                        MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
                        if (estado.cargando) CircularProgressIndicator()
                        else BotonPrincipal("Volver a intentar", onClick = modelo::recuperar)
                        TextButton(onClick = modelo::cerrarSesion) { Text("Cerrar sesión") }
                    }
            }
        }
    }
}
