package com.canadulce.app.ui.componentes

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val MentaOrbital = Color(0xFF9FFBE6)
val OroOrbital = Color(0xFFFFDC80)
val LilaOrbital = Color(0xFFC9BDFF)
private val Traje = Color(0xFFF4EFFF)
private val SombraTraje = Color(0xFFACA3F3)
private val Casco = Color(0xFF221A51)

fun permiteMovimiento(activado: Boolean): Boolean =
    activado && (Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled())

@Composable
fun EtiquetaOrbital(texto: String, color: Color = MentaOrbital) {
    Text(
        texto,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = .13f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = .7.sp,
    )
}

@Composable
fun HeroeOrbital(
    etiqueta: String,
    titulo: String,
    mensaje: String,
    movimiento: Boolean,
    modifier: Modifier = Modifier,
    grande: Boolean = false,
) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF261951), Color(0xFF3C2578), Color(0xFF164258)))),
    ) {
        Canvas(Modifier.matchParentSize()) {
            for (i in 0 until 36) {
                val x = size.width * ((i * 37 + 4) % 101) / 101f
                val y = size.height * ((i * 23 + 5) % 97) / 97f
                val color = if (i % 4 == 0) MentaOrbital else Color.White
                drawCircle(color.copy(alpha = .24f + (i % 5) * .1f), if (i % 4 == 0) 2.dp.toPx() else 1.dp.toPx(), Offset(x, y))
            }
            drawCircle(Color(0xFFAD87FF).copy(alpha = .13f), size.height * .67f, Offset(size.width * .91f, size.height * .43f))
            drawCircle(Color(0xFFAD87FF).copy(alpha = .18f), size.height * .52f, Offset(size.width * .91f, size.height * .43f), style = Stroke(1.dp.toPx()))
        }
        Row(
            Modifier.fillMaxWidth().heightIn(min = if (grande) 215.dp else 146.dp)
                .padding(start = 18.dp, end = 6.dp, top = 17.dp, bottom = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaOrbital(etiqueta)
                Text(
                    titulo,
                    color = Color.White,
                    style = if (grande) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(mensaje, color = Color(0xFFE3DAFF), style = MaterialTheme.typography.bodyMedium)
            }
            Astronauta(
                movimiento,
                Modifier.width(if (grande) 136.dp else 105.dp).height(if (grande) 174.dp else 125.dp),
            )
        }
    }
}

@Composable
fun Astronauta(movimiento: Boolean, modifier: Modifier = Modifier) {
    val vuelo = if (permiteMovimiento(movimiento)) {
        rememberInfiniteTransition(label = "Vuelo de Astro").animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "Flotación",
        )
    } else null
    Canvas(modifier) {
        val unidad = minOf(size.width / 116f, size.height / 135f)
        val fase = vuelo?.value ?: 0f
        withTransform({
            translate((size.width - unidad * 116f) / 2, (size.height - unidad * 135f) / 2)
            scale(unidad, unidad, Offset.Zero)
        }) {
            drawOval(
                LilaOrbital.copy(alpha = .34f), Offset(4f, 40f), Size(108f, 63f),
                style = Stroke(1.2f),
            )
            destello(Offset(12f, 37f), 5f, OroOrbital)
            destello(Offset(102f, 61f), 6f, MentaOrbital)
            destello(Offset(91f, 112f), 4f, LilaOrbital)
            drawCircle(OroOrbital, 2.6f, Offset(25f, 107f))
            withTransform({ translate(top = fase * 5f) }) { dibujarAstro(fase) }
        }
    }
}

private fun DrawScope.dibujarAstro(fase: Float) {
    drawRoundRect(Color(0xFF7865D4), Offset(32f, 63f), Size(54f, 38f), CornerRadius(13f))
    drawLine(SombraTraje, Offset(47f, 93f), Offset(43f, 112f), 15f, StrokeCap.Round)
    drawLine(Traje, Offset(69f, 93f), Offset(75f, 109f), 15f, StrokeCap.Round)
    drawRoundRect(Casco, Offset(32f, 111f), Size(22f, 10f), CornerRadius(5f))
    drawRoundRect(Casco, Offset(69f, 107f), Size(22f, 10f), CornerRadius(5f))
    drawLine(Traje, Offset(41f, 75f), Offset(24f, 84f), 13f, StrokeCap.Round)
    drawCircle(MentaOrbital, 7f, Offset(24f, 84f))
    drawLine(Traje, Offset(78f, 75f), Offset(94f, 63f + fase * 3f), 12f, StrokeCap.Round)
    drawCircle(MentaOrbital, 6.5f, Offset(94f, 63f + fase * 3f))
    drawRoundRect(Traje, Offset(39f, 63f), Size(42f, 38f), CornerRadius(14f))
    drawRoundRect(SombraTraje, Offset(39f, 89f), Size(42f, 12f), CornerRadius(6f))
    drawRoundRect(Color(0xFF7964D3), Offset(51f, 76f), Size(19f, 13f), CornerRadius(4f))
    drawCircle(MentaOrbital, 2.5f, Offset(56.5f, 82.5f))
    drawCircle(OroOrbital, 2.5f, Offset(64.5f, 82.5f))
    drawRoundRect(SombraTraje, Offset(24f, 24f), Size(68f, 47f), CornerRadius(22f))
    drawRoundRect(Traje, Offset(25f, 17f), Size(66f, 51f), CornerRadius(24f))
    drawCircle(SombraTraje, 7f, Offset(25f, 44f))
    drawCircle(SombraTraje, 7f, Offset(91f, 44f))
    drawRoundRect(
        Brush.linearGradient(listOf(Color(0xFF5446AF), Casco), Offset(36f, 26f), Offset(79f, 60f)),
        Offset(32f, 27f), Size(52f, 32f), CornerRadius(15f),
    )
    drawRoundRect(Color.White.copy(alpha = .40f), Offset(40f, 31f), Size(18f, 3f), CornerRadius(2f))
    drawLine(MentaOrbital, Offset(50f, 43f), Offset(50f, 47f), 3f, StrokeCap.Round)
    drawLine(MentaOrbital, Offset(67f, 43f), Offset(67f, 47f), 3f, StrokeCap.Round)
    drawArc(MentaOrbital, 0f, 180f, false, Offset(54f, 46f), Size(9f, 7f), style = Stroke(1.6f, cap = StrokeCap.Round))
}

internal fun DrawScope.destello(centro: Offset, radio: Float, color: Color, puntas: Int = 4) {
    val figura = Path()
    repeat(puntas * 2) { indice ->
        val angulo = indice * PI / puntas - PI / 2
        val distancia = if (indice % 2 == 0) radio else radio * .37f
        val x = centro.x + cos(angulo).toFloat() * distancia
        val y = centro.y + sin(angulo).toFloat() * distancia
        if (indice == 0) figura.moveTo(x, y) else figura.lineTo(x, y)
    }
    figura.close()
    drawPath(figura, color)
}

@Composable
fun PlanetaMision(variante: Int, modifier: Modifier = Modifier) {
    val color = when (variante % 3) {
        0 -> MentaOrbital
        1 -> LilaOrbital
        else -> OroOrbital
    }
    Canvas(modifier) {
        val centro = Offset(size.width * .5f, size.height * .5f)
        val radio = minOf(size.width, size.height) * .27f
        drawCircle(color.copy(alpha = .12f), radio * 1.35f, centro)
        drawCircle(
            Brush.linearGradient(listOf(color, color.copy(alpha = .45f)), centro - Offset(radio, radio), centro + Offset(radio, radio)),
            radio,
            centro,
        )
        drawCircle(Color(0xFF28204C).copy(alpha = .22f), radio * .28f, centro - Offset(radio * .34f, radio * .18f))
        drawCircle(Color.White.copy(alpha = .30f), radio * .13f, centro + Offset(radio * .37f, -radio * .38f))
        withTransform({ rotate(-27f, centro) }) {
            drawArc(color.copy(alpha = .83f), 0f, 180f, false, centro - Offset(radio * 1.54f, radio * .40f),
                Size(radio * 3.08f, radio * .8f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
        destello(Offset(size.width * .79f, size.height * .16f), 4.dp.toPx(), Color.White)
        drawCircle(color.copy(alpha = .8f), 1.6.dp.toPx(), Offset(size.width * .16f, size.height * .76f))
    }
}
