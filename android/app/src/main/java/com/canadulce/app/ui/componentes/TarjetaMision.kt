package com.canadulce.app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Lectura

@Composable
fun TarjetaMision(lectura: Lectura, onAbrir: () -> Unit, cargando: Boolean) {
    Card(onClick = onAbrir, enabled = !cargando, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    lectura.categoria,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    lectura.grado.toString() + ".º grado",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                lectura.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
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
                "Abrir historia →",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
