package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canadulce.app.datos.EstadoApp

@Composable
fun PantallaLectura(estado: EstadoApp, onVolver: () -> Unit, onTamano: (Int) -> Unit) {
    val lectura = estado.lectura ?: return
    val desplazamiento = rememberScrollState()
    val progreso =
        if (desplazamiento.maxValue <= 0 || desplazamiento.maxValue == Int.MAX_VALUE) 0f
        else (desplazamiento.value.toFloat() / desplazamiento.maxValue).coerceIn(0f, 1f)
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onVolver) { Text("‹ Volver a mis lecturas") }
        Text(
            lectura.categoria + " · " + lectura.grado + ".º grado",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            lectura.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Card(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Tamaño del texto",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                )
                OutlinedButton(
                    onClick = { onTamano(-2) },
                    enabled = estado.tamanoTexto > 18,
                    modifier = Modifier.semantics { contentDescription = "Reducir texto" },
                ) {
                    Text("A−")
                }
                Text(
                    estado.tamanoTexto.toString(),
                    modifier =
                        Modifier.semantics {
                            contentDescription = "Tamaño actual " + estado.tamanoTexto
                        },
                )
                OutlinedButton(
                    onClick = { onTamano(2) },
                    enabled = estado.tamanoTexto < 32,
                    modifier = Modifier.semantics { contentDescription = "Aumentar texto" },
                ) {
                    Text("A+")
                }
            }
        }
        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
        Column(
            Modifier.weight(1f).verticalScroll(desplazamiento).padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                lectura.contenido,
                fontSize = estado.tamanoTexto.sp,
                lineHeight = (estado.tamanoTexto * 1.65f).sp,
            )
            HorizontalDivider()
            Text(
                "Cada historia deja una semilla de imaginación.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
