package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Pantalla
import com.canadulce.app.datos.Pregunta
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaGestionLectura(estado: EstadoApp, modelo: PrincipalViewModel) {
    val lectura = estado.lectura ?: return
    var confirmarLectura by remember { mutableStateOf(false) }
    var confirmarEliminacion by rememberSaveable(lectura.id) { mutableStateOf(false) }
    var confirmarPregunta by remember { mutableStateOf<Pregunta?>(null) }
    val activa = lectura.estado == "ACTIVA"
    val preguntas = estado.gestion.preguntas
    val activas = preguntas.count { it.estado == "ACTIVA" }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = modelo::volver, enabled = !estado.cargando) {
                Text("‹ Área docente")
            }
            CabeceraDocente(
                "BIBLIOTECA · DETALLE DE LECTURA",
                lectura.titulo,
                "${lectura.grado}.º grado · Nivel ${lectura.dificultad.lowercase()}",
            )
        }
        item {
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
            if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        item {
            SeccionDocente(
                "Prepara tu misión",
                if (activa) "La lectura está disponible para el grupo."
                else "Completa las preguntas y publícala cuando esté lista.",
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoDocente(if (activa) "Publicada" else "Borrador o inactiva", activa)
                    EstadoDocente(
                        if (activas == 1) "1 pregunta activa" else "$activas preguntas activas",
                        true,
                    )
                }
                if (lectura.categoria.isNotBlank()) {
                    Text(
                        "Categoría · ${lectura.categoria}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (lectura.bloqueada) {
                    Text(
                        "Esta misión tiene intentos. El contenido y las preguntas se conservan para proteger el historial.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { modelo.ir(Pantalla.LECTURA) },
                        enabled = !estado.cargando,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    ) {
                        Text("Vista previa")
                    }
                    if (!lectura.bloqueada) {
                        OutlinedButton(
                            onClick = { modelo.editarLectura(false) },
                            enabled = !estado.cargando,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        ) {
                            Text("Editar lectura")
                        }
                    }
                }
                BotonPrincipal(
                    if (activa) "Desactivar lectura" else "Publicar misión",
                    cargando = estado.cargando,
                    onClick = {
                        if (activa) confirmarLectura = true else modelo.cambiarPublicacion()
                    },
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconoDocente(SimboloDocente.PREGUNTAS)
                Text(
                    "Cuestionario",
                    Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Revisa las opciones y la respuesta correcta de cada pregunta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!lectura.bloqueada) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { modelo.editarPregunta(null) },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                ) {
                    Text("Agregar pregunta")
                }
            }
        }
        if (preguntas.isEmpty()) {
            item {
                VacioDocente(
                    "El primer reto empieza aquí",
                    "Añade al menos una pregunta activa con una respuesta correcta para publicar la misión.",
                    SimboloDocente.PREGUNTAS,
                )
            }
        }
        items(preguntas, key = { it.id }) { pregunta ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "PREGUNTA ${pregunta.orden}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        EstadoDocente(
                            if (pregunta.estado == "ACTIVA") "Activa" else "Inactiva",
                            pregunta.estado == "ACTIVA",
                        )
                    }
                    Text(
                        pregunta.enunciado,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        pregunta.tipo.replace('_', ' ').lowercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    pregunta.opciones.forEachIndexed { indice, opcion ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color =
                                if (opcion.correcta) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    if (opcion.correcta) "✓" else ('A' + indice).toString(),
                                    fontWeight = FontWeight.Bold,
                                )
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(opcion.texto)
                                    if (opcion.correcta)
                                        Text(
                                            "Respuesta correcta",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                }
                            }
                        }
                    }
                    if (!lectura.bloqueada) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = { modelo.editarPregunta(pregunta) },
                                enabled = !estado.cargando,
                            ) {
                                Text("Editar pregunta")
                            }
                            if (pregunta.estado == "ACTIVA") {
                                TextButton(
                                    onClick = { confirmarPregunta = pregunta },
                                    enabled = !estado.cargando,
                                ) {
                                    Text("Desactivar pregunta")
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SeccionDocente(
                "Eliminar de la biblioteca",
                if (lectura.bloqueada)
                    "Esta lectura tiene intentos de estudiantes. Puedes desactivarla para retirarla del catálogo y conservar sus resultados."
                else
                    "Puedes borrar esta lectura y sus preguntas porque todavía no tiene intentos de estudiantes.",
            ) {
                OutlinedButton(
                    onClick = { confirmarEliminacion = true },
                    enabled = !estado.cargando && !lectura.bloqueada,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    border =
                        BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(
                                alpha = if (lectura.bloqueada) .3f else 1f,
                            ),
                        ),
                ) {
                    Text("Eliminar lectura")
                }
            }
        }
    }
    if (confirmarLectura) {
        ConfirmarDesactivacion(
            "¿Desactivar esta lectura?",
            "Se quitará de las misiones disponibles. Los resultados guardados y los intentos en curso se conservan.",
            { confirmarLectura = false },
            {
                confirmarLectura = false
                modelo.cambiarPublicacion()
            },
        )
    }
    if (confirmarEliminacion) {
        AlertDialog(
            onDismissRequest = { confirmarEliminacion = false },
            title = { Text("¿Eliminar esta lectura?") },
            text = {
                Text(
                    "Se borrará «${lectura.titulo}», junto con sus preguntas y opciones. Esta acción es permanente.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarEliminacion = false
                        modelo.eliminarLectura()
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Text("Confirmar eliminación")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminacion = false }) { Text("Cancelar") }
            },
        )
    }
    confirmarPregunta?.let { pregunta ->
        ConfirmarDesactivacion(
            "¿Desactivar esta pregunta?",
            "La pregunta dejará de aparecer en el cuestionario activo.",
            { confirmarPregunta = null },
            {
                confirmarPregunta = null
                modelo.desactivarPregunta(pregunta)
            },
        )
    }
}

@Composable
private fun ConfirmarDesactivacion(
    titulo: String,
    texto: String,
    cerrar: () -> Unit,
    aceptar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = aceptar) { Text("Confirmar desactivación") } },
        dismissButton = { TextButton(onClick = cerrar) { Text("Cancelar") } },
    )
}
