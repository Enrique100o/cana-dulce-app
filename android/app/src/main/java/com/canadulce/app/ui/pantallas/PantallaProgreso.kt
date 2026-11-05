package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Companero
import com.canadulce.app.datos.Intento
import com.canadulce.app.datos.puntos
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaProgreso(
    estado: EstadoApp,
    onVolver: () -> Unit,
    onRefrescar: () -> Unit,
    onResultado: (Intento) -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    val progreso = estado.progreso
    val docente = estado.sesion?.rol == "DOCENTE"
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onVolver, modifier = Modifier.weight(1f)) {
                    Text(if (docente) "‹ Estudiantes" else "‹ Mapa estelar")
                }
                TextButton(onClick = onRefrescar, enabled = !estado.cargando) { Text("Actualizar") }
            }
            MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
            if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        if (progreso != null) {
            val rango = when {
                progreso.misiones >= 3 -> "Explorador estelar"
                progreso.misiones >= 1 -> "Piloto lector"
                else -> "Cadete espacial"
            }
            item {
                HeroeOrbital(
                    "BITÁCORA ESTELAR",
                    if (docente) progreso.alias else "Mi progreso",
                    if (docente) "${progreso.grado}.º grado · $rango" else "$rango · ${progreso.alias}",
                    estado.movimiento && !docente,
                    companero = estado.companero ?: Companero.ASTRO,
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedidorProgreso(progreso.estrellas.toString(), "Estrellas", Modifier.weight(1f))
                    MedidorProgreso(progreso.misiones.toString(), "Misiones", Modifier.weight(1f))
                    MedidorProgreso(puntos(progreso.puntos), "Puntos", Modifier.weight(1f))
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Objetivo · Explora 3 misiones distintas", fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { progreso.misiones.coerceAtMost(3) / 3f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "${progreso.misiones.coerceAtMost(3)} de 3 misiones · " +
                                if (progreso.completados == 1) "1 intento terminado"
                                else "${progreso.completados} intentos terminados",
                        )
                        Text("Promedio: " + (progreso.promedio?.let(::puntos) ?: "—") +
                            " · Mejor: " + (progreso.mejor?.let(::puntos) ?: "—"))
                    }
                }
            }
            item { Text("Colección de insignias", style = MaterialTheme.typography.titleLarge) }
            items(progreso.insignias, key = { it.codigo }) { insignia ->
                val objetivo = when (insignia.codigo) {
                    "PRIMERA_AVENTURA" -> "Completa tu primera misión."
                    "EXPLORADOR_HISTORIAS" -> "Completa tres misiones distintas."
                    "LECTURA_BRILLANTE" -> "Alcanza 100 puntos en un intento."
                    else -> "Logro de lectura."
                }
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (insignia.obtenida) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlanetaMision(insignia.codigo.length, Modifier.size(54.dp))
                        Column(Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(insignia.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(if (insignia.obtenida) "Desbloqueada ✓" else objetivo)
                        }
                    }
                }
            }
        }
        if (progreso != null || estado.historial.isNotEmpty()) {
            item { Text("Historial de misiones", style = MaterialTheme.typography.titleLarge) }
            if (estado.historial.isEmpty() && !estado.cargando && estado.error == null) {
                item { Text("Aquí aparecerán los resultados de tus misiones completadas.") }
            }
            items(estado.historial, key = { it.id }) { intento ->
                Card(
                    onClick = { onResultado(intento) },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(intento.titulo, style = MaterialTheme.typography.titleMedium)
                        Text("${intento.puntaje?.let(::puntos) ?: "—"} puntos · ${intento.estrellas ?: 0} estrellas")
                        Text("Ver respuestas →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MedidorProgreso(valor: String, etiqueta: String, modifier: Modifier) {
    Card(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}
