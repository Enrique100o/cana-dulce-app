package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Pantalla
import com.canadulce.app.datos.Pregunta
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.componentes.BotonPrincipal
import com.canadulce.app.ui.componentes.MensajePantalla

@Composable
fun PantallaGestionLectura(estado: EstadoApp, modelo: PrincipalViewModel) {
    val lectura = estado.lectura ?: return
    var confirmarLectura by remember { mutableStateOf(false) }
    var confirmarPregunta by remember { mutableStateOf<Pregunta?>(null) }
    val activa = lectura.estado == "ACTIVA"
    val preguntas = estado.gestion.preguntas
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = modelo::volver, enabled = !estado.cargando) { Text("‹ Área docente") }
            Text(lectura.titulo, style = MaterialTheme.typography.headlineMedium)
            Text(
                if (activa) "Publicada · visible para ${lectura.grado}.º" else "Inactiva · solo visible para docentes",
                color = MaterialTheme.colorScheme.primary,
            )
        }
        item {
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
            if (lectura.bloqueada) {
                Text("Esta misión tiene intentos. El contenido y las preguntas se conservan para proteger el historial.")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { modelo.ir(Pantalla.LECTURA) },
                    enabled = !estado.cargando,
                ) { Text("Vista previa") }
                if (!lectura.bloqueada) {
                    TextButton(
                        onClick = { modelo.editarLectura(false) },
                        enabled = !estado.cargando,
                    ) { Text("Editar lectura") }
                }
            }
            BotonPrincipal(
                if (activa) "Desactivar lectura" else "Publicar misión",
                cargando = estado.cargando,
                onClick = { if (activa) confirmarLectura = true else modelo.cambiarPublicacion() },
            )
        }
        item {
            Text("Cuestionario", style = MaterialTheme.typography.titleLarge)
            if (preguntas.isEmpty()) {
                Text("Añade al menos una pregunta activa con una respuesta correcta para publicar la misión.")
            }
            if (!lectura.bloqueada) {
                OutlinedButton(
                    onClick = { modelo.editarPregunta(null) },
                    enabled = !estado.cargando,
                ) { Text("Agregar pregunta") }
            }
        }
        items(preguntas, key = { it.id }) { pregunta ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "PREGUNTA ${pregunta.orden} · ${pregunta.estado}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(pregunta.enunciado, style = MaterialTheme.typography.titleMedium)
                    pregunta.opciones.forEach {
                        Text(
                            if (it.correcta) "✓ ${it.texto} · Correcta" else "• ${it.texto}",
                            fontWeight = if (it.correcta) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                    if (!lectura.bloqueada) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { modelo.editarPregunta(pregunta) },
                                enabled = !estado.cargando,
                            ) { Text("Editar pregunta") }
                            if (pregunta.estado == "ACTIVA") {
                                TextButton(
                                    onClick = { confirmarPregunta = pregunta },
                                    enabled = !estado.cargando,
                                ) { Text("Desactivar pregunta") }
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirmarLectura) {
        ConfirmarDesactivacion(
            "¿Desactivar esta lectura?",
            "Se quitará de las misiones disponibles. Los resultados guardados y los intentos en curso se conservan.",
            { confirmarLectura = false },
            { confirmarLectura = false; modelo.cambiarPublicacion() },
        )
    }
    confirmarPregunta?.let { pregunta ->
        ConfirmarDesactivacion(
            "¿Desactivar esta pregunta?",
            "La pregunta dejará de aparecer en el cuestionario activo.",
            { confirmarPregunta = null },
            { confirmarPregunta = null; modelo.desactivarPregunta(pregunta) },
        )
    }
}

@Composable
private fun ConfirmarDesactivacion(titulo: String, texto: String, cerrar: () -> Unit, aceptar: () -> Unit) {
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = aceptar) { Text("Confirmar desactivación") } },
        dismissButton = { TextButton(onClick = cerrar) { Text("Cancelar") } },
    )
}
