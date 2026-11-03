package com.canadulce.app.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibroAventurero(modifier: Modifier = Modifier) {
    Canvas(
        modifier.size(100.dp).semantics {
            contentDescription = "Libro aventurero con una pequeña planta"
        },
    ) {
        scale(size.width / 120f, size.height / 120f, pivot = Offset.Zero) {
            drawCircle(Color(0xFF66B88B).copy(alpha = .35f), 53f, Offset(60f, 60f))
            drawRoundRect(Color(0xFFFFC857), Offset(29f, 40f), Size(73f, 68f), CornerRadius(15f))
            drawRoundRect(Color(0xFFFDF8E9), Offset(20f, 30f), Size(72f, 68f), CornerRadius(13f))
            drawRoundRect(Color(0xFFA9D9B5), Offset(20f, 30f), Size(14f, 68f), CornerRadius(5f))
            drawCircle(Color(0xFF155B44), 3f, Offset(51f, 59f))
            drawCircle(Color(0xFF155B44), 3f, Offset(74f, 59f))
            drawCircle(Color(0xFFFFB7A3), 4f, Offset(42f, 70f))
            drawCircle(Color(0xFFFFB7A3), 4f, Offset(82f, 70f))
            drawArc(
                Color(0xFF155B44),
                15f,
                150f,
                false,
                Offset(52f, 65f),
                Size(21f, 14f),
                style = Stroke(3f),
            )
            drawLine(Color(0xFF155B44), Offset(67f, 31f), Offset(72f, 13f), 4f)
            val hoja =
                Path().apply {
                    moveTo(71f, 23f)
                    cubicTo(84f, 25f, 96f, 10f, 88f, 7f)
                    cubicTo(79f, 5f, 70f, 12f, 71f, 23f)
                }
            drawPath(hoja, Color(0xFF9CDD9B))
            drawCircle(Color(0xFFFFC857), 4f, Offset(13f, 23f))
            drawCircle(Color(0xFFFDF8E9), 3f, Offset(107f, 35f))
        }
    }
}

@Composable
fun EncabezadoAventura(titulo: String, descripcion: String, etiqueta: String = "CAÑA DULCE") {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF155B44), Color(0xFF238261))))
                .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(etiqueta, color = Color(0xFFFFD577), style = MaterialTheme.typography.labelLarge)
            Text(
                titulo,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                descripcion,
                color = Color(0xFFDCF1DE),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LibroAventurero()
    }
}

@Composable
fun PaginaFormulario(
    cargando: Boolean,
    onVolver: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TextButton(onClick = onVolver, enabled = !cargando) { Text("‹ Volver") }
        contenido()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun MensajePantalla(error: String?, aviso: String?, onCerrar: () -> Unit) {
    val mensaje = error ?: aviso ?: return
    Surface(
        color =
            if (error != null) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(mensaje, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onCerrar) { Text("Entendido") }
        }
    }
}

@Composable
fun BotonPrincipal(texto: String, cargando: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text("Conectando…")
        } else Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
