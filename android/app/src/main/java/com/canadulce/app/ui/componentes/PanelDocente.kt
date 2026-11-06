package com.canadulce.app.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SimboloDocente {
    LIBRO,
    ESTUDIANTES,
    PREGUNTAS,
}

@Composable
fun IconoDocente(
    simbolo: SimboloDocente,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier.size(24.dp)) {
        scale(size.width / 24f, size.height / 24f, Offset.Zero) {
            val trazo = Stroke(1.7f, cap = StrokeCap.Round)
            when (simbolo) {
                SimboloDocente.LIBRO -> {
                    drawRoundRect(
                        color,
                        Offset(3f, 4f),
                        Size(18f, 16f),
                        CornerRadius(2f),
                        style = trazo,
                    )
                    drawLine(color, Offset(12f, 5f), Offset(12f, 19f), 1.7f)
                    drawLine(color, Offset(6f, 8f), Offset(9f, 8f), 1.4f)
                    drawLine(color, Offset(15f, 8f), Offset(18f, 8f), 1.4f)
                    drawLine(color, Offset(6f, 12f), Offset(9f, 12f), 1.4f)
                }
                SimboloDocente.ESTUDIANTES -> {
                    drawCircle(color, 3f, Offset(9f, 8f), style = trazo)
                    drawCircle(color, 2.5f, Offset(17f, 9f), style = trazo)
                    drawArc(
                        color,
                        180f,
                        180f,
                        false,
                        Offset(3f, 13f),
                        Size(12f, 11f),
                        style = trazo,
                    )
                    drawArc(color, 230f, 130f, false, Offset(13f, 14f), Size(9f, 8f), style = trazo)
                }
                SimboloDocente.PREGUNTAS -> {
                    drawRoundRect(
                        color,
                        Offset(5f, 3f),
                        Size(14f, 18f),
                        CornerRadius(2f),
                        style = trazo,
                    )
                    drawLine(color, Offset(8f, 8f), Offset(16f, 8f), 1.7f)
                    drawLine(color, Offset(8f, 12f), Offset(16f, 12f), 1.7f)
                    drawLine(color, Offset(8f, 16f), Offset(12f, 16f), 1.7f)
                }
            }
        }
    }
}

@Composable
fun CabeceraDocente(etiqueta: String, titulo: String, descripcion: String) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF173D49), Color(0xFF275E60), Color(0xFF38665B)),
                ),
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                etiqueta,
                color = Color(0xFFBEEBDD),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.sp,
            )
            Text(
                titulo,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                descripcion,
                color = Color(0xFFDCEEE9),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LibroAventurero(Modifier.size(68.dp))
    }
}

@Composable
fun IndicadorDocente(
    valor: String,
    etiqueta: String,
    simbolo: SimboloDocente,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            IconoDocente(simbolo)
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EstadoDocente(texto: String, activo: Boolean) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color =
            if (activo) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            texto,
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color =
                if (activo) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SeccionDocente(
    titulo: String,
    descripcion: String,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            contenido()
        }
    }
}

@Composable
fun VacioDocente(titulo: String, descripcion: String, simbolo: SimboloDocente) {
    SeccionDocente(titulo, descripcion) { IconoDocente(simbolo, Modifier.size(40.dp)) }
}
