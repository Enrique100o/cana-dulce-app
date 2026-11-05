package com.canadulce.app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Lectura

@Composable
fun TarjetaMision(lectura: Lectura, onAbrir: () -> Unit, cargando: Boolean, espacial: Boolean = false) {
    Card(onClick = onAbrir, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    lectura.categoria.ifBlank { "Historias" },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    lectura.grado.toString() + ".º grado",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (espacial) PlanetaMision(lectura.id, Modifier.size(60.dp).padding(end = 6.dp))
                Text(
                    lectura.titulo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                when (lectura.dificultad) {
                    "BAJA" -> "Una aventura para comenzar"
                    "MEDIA" -> "Un nuevo reto de lectura"
                    else -> "Una aventura para ir más lejos"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Dificultad: " +
                    lectura.dificultad.lowercase().replaceFirstChar { it.uppercase() } +
                    " · " +
                    lectura.totalPreguntas +
                    if (lectura.totalPreguntas == 1) " pregunta" else " preguntas",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                if (espacial) "Explorar planeta →" else "Gestionar lectura →",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
