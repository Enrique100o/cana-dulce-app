package com.canadulce.app.ui.pantallas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Companero
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaCuestionario(
    estado: EstadoApp,
    onVolver: () -> Unit,
    onReleer: () -> Unit,
    onElegir: (Int, Int) -> Unit,
    onPaso: (Int) -> Unit,
    onFinalizar: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    val indice = estado.pasoPregunta
    val pregunta = estado.preguntas.getOrNull(indice) ?: return
    val total = estado.preguntas.size
    val seleccionada = estado.selecciones[pregunta.id]
    val companero = estado.companero ?: Companero.ASTRO
    val combustible by animateFloatAsState(
        estado.selecciones.size.toFloat() / total,
        animationSpec = if (permiteMovimiento(estado.movimiento)) tween(400) else snap(),
        label = "Combustible de la misión",
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onVolver, enabled = !estado.cargando, modifier = Modifier.weight(1f)) {
                Text("‹ Volver a mis lecturas")
            }
            TextButton(onClick = onReleer, enabled = !estado.cargando) { Text("Releer historia") }
        }
        Text(
            "${estado.selecciones.size} de $total respondidas",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { combustible },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MentaOrbital,
        )
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        key(pregunta.id) {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    HeroeOrbital(
                        "PREGUNTA ${indice + 1} DE $total",
                        "El reto de la historia",
                        if (seleccionada == null) "${companero.nombre} te invita a leer todas las opciones."
                        else "Guardé tu elección. Puedes cambiarla antes de enviar.",
                        estado.movimiento,
                        companero = companero,
                    )
                }
                item {
                    Text(pregunta.enunciado, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                item {
                    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        pregunta.opciones.forEachIndexed { numero, opcion ->
                            val activa = seleccionada == opcion.id
                            val forma = RoundedCornerShape(16.dp)
                            val color = if (activa) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 58.dp).clip(forma)
                                    .background(color)
                                    .border(1.dp, if (activa) MentaOrbital else MaterialTheme.colorScheme.outlineVariant, forma)
                                    .selectable(activa, enabled = !estado.cargando, role = Role.RadioButton) {
                                        onElegir(pregunta.id, opcion.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                EtiquetaOrbital(('A' + numero).toString(), if (activa) MentaOrbital else LilaOrbital)
                                Text(opcion.texto, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                                if (activa) Text("●", color = MentaOrbital)
                            }
                        }
                    }
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (indice > 0) {
                OutlinedButton(onClick = { onPaso(indice - 1) }, enabled = !estado.cargando) {
                    Text("Anterior")
                }
            }
            Box(Modifier.weight(1f)) {
                if (indice == total - 1) {
                    BotonPrincipal("Finalizar misión", cargando = estado.cargando, onClick = onFinalizar)
                } else {
                    BotonPrincipal("Siguiente pregunta", onClick = { onPaso(indice + 1) })
                }
            }
        }
    }
}
