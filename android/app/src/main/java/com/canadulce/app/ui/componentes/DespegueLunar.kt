package com.canadulce.app.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Companero
import kotlin.math.sin

@Composable
fun DespegueLunar(companero: Companero, movimiento: Boolean, alAbrir: Boolean) {
    val animar = permiteMovimiento(movimiento)
    var repeticion by rememberSaveable { mutableIntStateOf(0) }
    var visto by rememberSaveable { mutableStateOf(!alAbrir) }
    val viaje = remember { Animatable(1f) }
    LaunchedEffect(repeticion, animar) {
        if (animar && !visto) {
            viaje.snapTo(0f)
            viaje.animateTo(1f, tween(2850, easing = LinearEasing))
        } else viaje.snapTo(1f)
        visto = true
    }
    val llego by remember { derivedStateOf { viaje.value >= .999f } }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A2354), Color(0xFF332B65))))
            .padding(14.dp),
    ) {
        EtiquetaOrbital("MISIÓN PERFECTA", OroOrbital)
        Canvas(Modifier.fillMaxWidth().height(141.dp)) {
            val t = viaje.value
            val inicio = Offset(size.width * .15f, size.height * .83f)
            val meta = Offset(size.width * .79f, size.height * .27f)
            val curva = Offset(size.width * .37f, size.height * .02f)
            val luna = Offset(size.width * .86f, size.height * .30f)
            val radio = minOf(size.height, size.width) * .21f
            for (i in 0 until 26) {
                val x = size.width * ((i * 37 + 9) % 97) / 97f
                val y = size.height * ((i * 29 + 3) % 89) / 89f
                drawCircle(LilaOrbital.copy(alpha = .35f + .1f * (i % 4)), (1 + i % 2).dp.toPx(), Offset(x, y))
            }
            drawCircle(LilaOrbital.copy(alpha = .17f), radio * 1.3f, luna)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFF8EEFF), Color(0xFFAA9BCC)), luna, radio), radio, luna)
            drawCircle(Color(0xFF9586B1), radio * .18f, luna + Offset(-radio * .24f, -radio * .23f))
            drawCircle(Color(0xFF9E90B8), radio * .12f, luna + Offset(radio * .27f, radio * .30f))
            drawCircle(Color(0xFF8E83B2), radio * .08f, luna + Offset(radio * .34f, -radio * .33f))
            drawOval(Color(0xFF6760AC), inicio - Offset(37.dp.toPx(), -13.dp.toPx()),
                Size(74.dp.toPx(), 13.dp.toPx()))
            val ruta = Path().apply { moveTo(inicio.x, inicio.y); quadraticTo(curva.x, curva.y, meta.x, meta.y) }
            drawPath(ruta, LilaOrbital.copy(alpha = .48f),
                style = Stroke(1.3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 6.dp.toPx()))))
            val q = 1f - t
            val nave = inicio * (q * q) + curva * (2f * q * t) + meta * (t * t)
            val inclinacion = if (t >= .999f) 12f else 23f + 40f * t
            val escala = 1.dp.toPx() * (1.05f - .22f * t)
            withTransform({
                translate(nave.x, nave.y)
                rotate(inclinacion, Offset.Zero)
                scale(escala, escala, Offset.Zero)
            }) { dibujarCohete(companero, t) }
            if (t >= .999f) destello(luna - Offset(radio * .6f, radio * .8f), 9.dp.toPx(), OroOrbital)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (llego) "¡Llegaste a la Luna!" else "¡Despegando hacia la Luna!",
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
            if (animar && llego) {
                TextButton(onClick = { visto = false; repeticion++ }) {
                    Text("Ver despegue", color = MentaOrbital)
                }
            }
        }
    }
}

private fun DrawScope.dibujarCohete(companero: Companero, fase: Float) {
    val acento = if (companero == Companero.LINA) Color(0xFFFFB071) else MentaOrbital
    if (fase < .98f) {
        val largo = 16f + 5f * sin(fase * 48)
        drawPath(Path().apply {
            moveTo(-7f, 18f); quadraticTo(-8f, 32f, 0f, 18f + largo)
            quadraticTo(8f, 32f, 7f, 18f); close()
        }, OroOrbital)
    }
    drawPath(Path().apply {
        moveTo(-9f, 5f); lineTo(-17f, 20f); lineTo(17f, 20f); lineTo(9f, 5f); close()
    }, acento)
    drawPath(Path().apply {
        moveTo(0f, -23f); cubicTo(-13f, -13f, -13f, 1f, -9f, 18f)
        lineTo(9f, 18f); cubicTo(13f, 1f, 13f, -13f, 0f, -23f); close()
    }, Color(0xFFF3EFFF))
    drawCircle(Color(0xFF383063), 6.5f, Offset(0f, -3f))
    drawCircle(acento, 4.6f, Offset(0f, -3f))
    drawCircle(Color.White.copy(alpha = .65f), 1.5f, Offset(-1.5f, -4.5f))
}
