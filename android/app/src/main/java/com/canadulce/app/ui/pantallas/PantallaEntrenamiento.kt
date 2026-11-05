package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Companero
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*

private data class Reto(
    val pregunta: String, val opciones: List<String>, val correcta: Int, val pista: String,
)

private val retos = listOf(
    Reto(
        "¿Qué llevó Lina al refugio?",
        listOf("Una brújula", "Una caja de semillas", "Una roca"),
        1, "Busca qué objeto iba dentro de la mochila.",
    ),
    Reto(
        "¿Qué hicieron antes de abrir la caja?",
        listOf("Buscaron sombra", "Cerraron el refugio", "Volvieron a la nave"),
        0, "Vuelve a la frase que empieza con «Primero».",
    ),
    Reto(
        "¿Por qué buscaron sombra?",
        listOf("Para ver las estrellas", "Para encontrar agua", "Para cuidar las semillas del calor"),
        2, "Lee cómo estaba el suelo y qué podían necesitar las semillas.",
    ),
)

@Composable
fun PantallaEntrenamiento(estado: EstadoApp, onVolver: () -> Unit) {
    val companero = estado.companero ?: Companero.ASTRO
    var indice by rememberSaveable { mutableIntStateOf(0) }
    var eleccion by rememberSaveable { mutableIntStateOf(-1) }
    var pulso by rememberSaveable { mutableIntStateOf(0) }
    val reto = retos[indice]
    val acierto = eleccion == reto.correcta
    val lista = rememberLazyListState()
    LaunchedEffect(pulso, eleccion) {
        if (eleccion >= 0) {
            val indiceMensaje = 4 + reto.opciones.size
            if (permiteMovimiento(estado.movimiento)) lista.animateScrollToItem(indiceMensaje)
            else lista.scrollToItem(indiceMensaje)
        }
    }
    val gesto = when {
        eleccion == -1 -> GestoOrbital.FLOTAR
        acierto -> GestoOrbital.CELEBRAR
        else -> GestoOrbital.ANIMAR
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        state = lista,
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TextButton(onClick = onVolver) { Text("‹ Mapa estelar") } }
        item {
            HeroeOrbital(
                "PRÁCTICA ${indice + 1} DE ${retos.size}",
                "Entrena con ${companero.nombre}",
                "Ensaya a tu ritmo. Los puntos se ganan en las misiones.",
                estado.movimiento,
                companero = companero,
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Las semillas del refugio", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Lina llevó una caja de semillas en su mochila. El suelo del refugio estaba caliente. " +
                            "Primero buscó sombra junto con Astro. Allí abrieron la caja para cuidar las semillas del calor.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        item { Text(reto.pregunta, style = MaterialTheme.typography.titleLarge) }
        reto.opciones.forEachIndexed { i, opcion ->
            item {
                OutlinedButton(
                    onClick = { eleccion = i; pulso++ },
                    enabled = !acierto,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                ) { Text(opcion) }
            }
        }
        if (eleccion >= 0) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PersonajeOrbital(companero, estado.movimiento, Modifier.size(94.dp), gesto, pulso)
                        Column(Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                if (acierto) "¡Acertaste!" else "Probemos otra vez",
                                color = if (acierto) MentaOrbital else OroOrbital,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                            )
                            Text(if (acierto) "¡Encontraste la pista en la historia!" else reto.pista)
                        }
                    }
                }
            }
        }
        if (acierto) {
            item {
                val ultimo = indice == retos.lastIndex
                BotonPrincipal(if (ultimo) "Completar entrenamiento" else "Siguiente reto") {
                    if (ultimo) onVolver()
                    else { indice++; eleccion = -1 }
                }
            }
        }
    }
    LaunchedEffect(indice) {
        if (eleccion == -1) lista.scrollToItem(0)
    }
}
