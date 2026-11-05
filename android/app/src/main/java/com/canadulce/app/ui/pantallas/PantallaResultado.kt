package com.canadulce.app.ui.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Pantalla
import com.canadulce.app.datos.puntos
import com.canadulce.app.ui.componentes.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PantallaResultado(estado: EstadoApp, onVolver: () -> Unit, onProgreso: () -> Unit) {
    val resultado = estado.resultado ?: return
    val animar = permiteMovimiento(estado.movimiento) && estado.regresoResultado == Pantalla.MISIONES
    val celebracion = remember(resultado, animar) { Animatable(if (animar) 0f else 1f) }
    LaunchedEffect(celebracion) {
        if (animar) celebracion.animateTo(1f, tween(1550))
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HeroeOrbital(
                "REGRESO A LA BASE",
                "¡Misión completada!",
                if (resultado.estrellas >= 2) "¡Tu lectura iluminó la galaxia!"
                else "Astro celebra tu esfuerzo. Cada misión te ayuda a mejorar.",
                estado.movimiento,
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth()) {
                    if (animar) {
                        Canvas(Modifier.matchParentSize()) {
                            val fase = celebracion.value
                            for (i in 0 until 24) {
                                val angulo = i * 2.39996
                                val alcance = (.25f + (i % 5) * .12f) * fase
                                val x = size.width * .5f + cos(angulo).toFloat() * size.width * alcance
                                val y = size.height * .48f + sin(angulo).toFloat() * size.height * alcance + fase * fase * 26.dp.toPx()
                                val color = when (i % 3) {
                                    0 -> OroOrbital
                                    1 -> MentaOrbital
                                    else -> LilaOrbital
                                }
                                destello(Offset(x, y), (3 + i % 4).dp.toPx(), color.copy(alpha = (1f - fase) * .8f))
                            }
                        }
                    }
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            Modifier.semantics { contentDescription = "${resultado.estrellas} estrellas" },
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            repeat(3) { i ->
                                val ganada = i < resultado.estrellas
                                Text(
                                    if (ganada) "★" else "☆",
                                    modifier = Modifier.graphicsLayer {
                                        val avance = (celebracion.value * 2f - i * .35f).coerceIn(0f, 1f)
                                        val escala = if (ganada) .45f + avance * .55f else 1f
                                        scaleX = escala
                                        scaleY = escala
                                        alpha = if (ganada) .4f + avance * .6f else 1f
                                    },
                                    color = if (ganada) OroOrbital else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.displaySmall,
                                )
                            }
                        }
                        Text(
                            puntos(resultado.puntaje) + " puntos",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text("${resultado.correctas} de ${resultado.total} respuestas correctas")
                    }
                }
            }
        }
        if (estado.premiosNuevos.isNotEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlanetaMision(2, Modifier.size(58.dp))
                        Column(Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            EtiquetaOrbital("INSIGNIAS DESBLOQUEADAS", OroOrbital)
                            estado.premiosNuevos.forEach { Text(it.nombre, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
        item {
            if (estado.regresoResultado == Pantalla.MISIONES) {
                BotonPrincipal("Volver a misiones", onClick = onVolver)
                TextButton(onClick = onProgreso, modifier = Modifier.fillMaxWidth()) { Text("Mi progreso") }
            } else BotonPrincipal("Volver al progreso", onClick = onVolver)
        }
        item { Text("Revisa tus respuestas", style = MaterialTheme.typography.titleLarge) }
        itemsIndexed(resultado.respuestas) { indice, respuesta ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EtiquetaOrbital(
                        if (respuesta.correcta) "✓ ACIERTO" else "↺ PARA PRACTICAR",
                        if (respuesta.correcta) MentaOrbital else OroOrbital,
                    )
                    Text("${indice + 1}. ${respuesta.enunciado}", fontWeight = FontWeight.SemiBold)
                    Text("Tu respuesta: ${respuesta.elegida}")
                }
            }
        }
    }
}
