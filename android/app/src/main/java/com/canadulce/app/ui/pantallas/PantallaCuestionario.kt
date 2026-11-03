package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*
import java.util.Locale

@Composable
fun PantallaCuestionario(
    estado: EstadoApp,
    onVolver: () -> Unit,
    onReleer: () -> Unit,
    onElegir: (Int, Int) -> Unit,
    onFinalizar: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(onClick = onVolver) { Text("‹ Volver a mis lecturas") }
        Text(
            "El reto de la historia",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(estado.lectura?.titulo.orEmpty(), style = MaterialTheme.typography.titleMedium)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${estado.selecciones.size} de ${estado.preguntas.size} respondidas")
            TextButton(onClick = onReleer, enabled = !estado.cargando) { Text("Releer historia") }
        }
        LinearProgressIndicator(
            progress = {
                if (estado.preguntas.isEmpty()) 0f
                else estado.selecciones.size.toFloat() / estado.preguntas.size
            },
            modifier = Modifier.fillMaxWidth(),
        )
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            itemsIndexed(estado.preguntas, key = { _, pregunta -> pregunta.id }) { indice, pregunta
                ->
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "Pregunta ${indice + 1}",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(pregunta.enunciado, style = MaterialTheme.typography.titleMedium)
                        Column(
                            Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            pregunta.opciones.forEach { opcion ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .selectable(
                                            selected = estado.selecciones[pregunta.id] == opcion.id,
                                            enabled = !estado.cargando,
                                            role = Role.RadioButton,
                                            onClick = { onElegir(pregunta.id, opcion.id) },
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = estado.selecciones[pregunta.id] == opcion.id,
                                        onClick = null,
                                    )
                                    Text(
                                        opcion.texto,
                                        modifier = Modifier.padding(start = 10.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        BotonPrincipal("Finalizar misión", cargando = estado.cargando, onClick = onFinalizar)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun PantallaResultado(estado: EstadoApp, onVolver: () -> Unit) {
    val resultado = estado.resultado ?: return
    PaginaFormulario(cargando = false, onVolver = onVolver) {
        EncabezadoAventura(
            "¡Misión completada!",
            "Cada historia te ayuda a crecer. Sigue descubriendo nuevas aventuras.",
            "TU ESFUERZO CUENTA",
        )
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "★".repeat(resultado.estrellas) + "☆".repeat(3 - resultado.estrellas),
                    modifier =
                        Modifier.semantics {
                            contentDescription = "${resultado.estrellas} estrellas"
                        },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    String.format(Locale.forLanguageTag("es-CO"), "%.2f puntos", resultado.puntaje),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text("${resultado.correctas} de ${resultado.total} respuestas correctas")
                Text(
                    if (resultado.puntaje >= 80) "¡Excelente lectura!"
                    else "Vuelve a leer con calma: cada intento es una oportunidad para aprender.",
                )
            }
        }
        BotonPrincipal("Volver a misiones", onClick = onVolver)
    }
}
