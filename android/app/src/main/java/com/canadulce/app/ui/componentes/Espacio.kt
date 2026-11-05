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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.canadulce.app.datos.Companero
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val MentaOrbital = Color(0xFF9FFBE6)
val OroOrbital = Color(0xFFFFDC80)
val LilaOrbital = Color(0xFFC9BDFF)
private val Traje = Color(0xFFF4EFFF)
private val SombraTraje = Color(0xFFACA3F3)
private val Casco = Color(0xFF221A51)

enum class GestoOrbital { FLOTAR, CELEBRAR, ANIMAR }

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
    companero: Companero = Companero.ASTRO,
    gesto: GestoOrbital = GestoOrbital.FLOTAR,
    pulso: Int = 0,
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
            PersonajeOrbital(
                companero,
                movimiento,
                Modifier.width(if (grande) 136.dp else 105.dp).height(if (grande) 174.dp else 125.dp),
                gesto,
                pulso,
            )
        }
    }
}

@Composable
fun PersonajeOrbital(
    companero: Companero,
    movimiento: Boolean,
    modifier: Modifier = Modifier,
    gesto: GestoOrbital = GestoOrbital.FLOTAR,
    pulso: Int = 0,
) {
    val animar = permiteMovimiento(movimiento)
    val vuelo = if (animar && gesto == GestoOrbital.FLOTAR) {
        rememberInfiniteTransition(label = "Vuelo de Astro").animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "Flotación",
        )
    } else null
    val reaccion = remember { Animatable(0f) }
    LaunchedEffect(gesto, pulso, animar) {
        reaccion.snapTo(0f)
        if (animar && gesto != GestoOrbital.FLOTAR) {
            reaccion.animateTo(1f, tween(gesto.duracion()))
        }
    }
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
            val onda = sin(reaccion.value * PI).toFloat()
            val salto = if (gesto == GestoOrbital.CELEBRAR) -12f * onda else 0f
            val giro = if (gesto == GestoOrbital.ANIMAR) -9f * onda else 0f
            withTransform({
                translate(top = fase * 5f + salto)
                rotate(giro, Offset(58f, 72f))
            }) {
                dibujarCompanero(companero, fase, gesto)
            }
            if (gesto == GestoOrbital.CELEBRAR) {
                destello(Offset(15f, 25f - onda * 5), 8f, OroOrbital, 5)
                destello(Offset(102f, 31f - onda * 6), 6f, MentaOrbital, 5)
            } else if (gesto == GestoOrbital.ANIMAR) {
                dibujarPista(Offset(99f, 35f - onda * 4))
            }
        }
    }
}

private fun GestoOrbital.duracion(): Int = if (this == GestoOrbital.CELEBRAR) 650 else 780

private fun DrawScope.dibujarCompanero(companero: Companero, fase: Float, gesto: GestoOrbital) {
    val lina = companero == Companero.LINA
    val sombra = if (lina) Color(0xFF76C6C0) else SombraTraje
    val acento = if (lina) Color(0xFFFFB071) else MentaOrbital
    val equipo = if (lina) Color(0xFF347A85) else Color(0xFF7865D4)
    val celebra = gesto == GestoOrbital.CELEBRAR
    val orienta = gesto == GestoOrbital.ANIMAR
    drawRoundRect(equipo, Offset(32f, 63f), Size(54f, 38f), CornerRadius(13f))
    drawLine(sombra, Offset(47f, 93f), Offset(43f, 112f), 15f, StrokeCap.Round)
    drawLine(Traje, Offset(69f, 93f), Offset(75f, 109f), 15f, StrokeCap.Round)
    drawRoundRect(Casco, Offset(32f, 111f), Size(22f, 10f), CornerRadius(5f))
    drawRoundRect(Casco, Offset(69f, 107f), Size(22f, 10f), CornerRadius(5f))
    val manoIzquierda = if (celebra) Offset(19f, 55f) else Offset(24f, 84f)
    val manoDerecha = when {
        celebra -> Offset(99f, 52f)
        orienta -> Offset(96f, 50f)
        else -> Offset(94f, 63f + fase * 3f)
    }
    drawLine(Traje, Offset(41f, 75f), manoIzquierda, 13f, StrokeCap.Round)
    drawCircle(acento, 7f, manoIzquierda)
    drawLine(Traje, Offset(78f, 75f), manoDerecha, 12f, StrokeCap.Round)
    drawCircle(acento, 6.5f, manoDerecha)
    drawRoundRect(Traje, Offset(39f, 63f), Size(42f, 38f), CornerRadius(14f))
    drawRoundRect(sombra, Offset(39f, 89f), Size(42f, 12f), CornerRadius(6f))
    drawRoundRect(equipo, Offset(51f, 76f), Size(19f, 13f), CornerRadius(4f))
    drawCircle(acento, 2.5f, Offset(56.5f, 82.5f))
    drawCircle(OroOrbital, 2.5f, Offset(64.5f, 82.5f))
    drawRoundRect(sombra, Offset(24f, 24f), Size(68f, 47f), CornerRadius(22f))
    drawRoundRect(Traje, Offset(25f, 17f), Size(66f, 51f), CornerRadius(24f))
    drawCircle(sombra, 7f, Offset(25f, 44f))
    drawCircle(sombra, 7f, Offset(91f, 44f))
    drawRoundRect(
        Brush.linearGradient(listOf(equipo, Casco), Offset(36f, 26f), Offset(79f, 60f)),
        Offset(32f, 27f), Size(52f, 32f), CornerRadius(15f),
    )
    if (lina) {
        drawOval(Color(0xFF39213D), Offset(39f, 28f), Size(40f, 31f))
        drawOval(Color(0xFFD4956A), Offset(46f, 33f), Size(26f, 25f))
        val flequillo = Path().apply {
            moveTo(43f, 42f)
            quadraticTo(43f, 27f, 62f, 30f)
            quadraticTo(73f, 30f, 74f, 41f)
            quadraticTo(64f, 41f, 60f, 35f)
            quadraticTo(54f, 43f, 43f, 42f)
            close()
        }
        drawPath(flequillo, Color(0xFF39213D))
        drawCircle(Color(0xFFECAB92), 2.4f, Offset(49f, 49f))
        drawCircle(Color(0xFFECAB92), 2.4f, Offset(69f, 49f))
    }
    drawRoundRect(Color.White.copy(alpha = .40f), Offset(39f, 31f), Size(10f, 2.5f), CornerRadius(2f))
    val ojos = if (lina) Color(0xFF382239) else MentaOrbital
    if (celebra) {
        listOf(50f, 67f).forEach { x ->
            drawArc(ojos, 185f, 170f, false, Offset(x - 3, 42f), Size(6f, 5f),
                style = Stroke(1.7f, cap = StrokeCap.Round))
        }
    } else {
        drawLine(ojos, Offset(50f, 43f), Offset(50f, 46f), 2.6f, StrokeCap.Round)
        drawLine(ojos, Offset(67f, 43f), Offset(67f, 46f), 2.6f, StrokeCap.Round)
    }
    drawArc(ojos, 0f, 180f, false, Offset(54f, 46f), Size(9f, if (celebra) 9f else 6f),
        style = Stroke(1.6f, cap = StrokeCap.Round))
}

private fun DrawScope.dibujarPista(centro: Offset) {
    drawCircle(OroOrbital.copy(alpha = .22f), 13f, centro)
    drawCircle(OroOrbital, 6f, centro)
    drawRoundRect(OroOrbital, centro + Offset(-3f, 5f), Size(6f, 3f), CornerRadius(1f))
    drawLine(OroOrbital, centro + Offset(0f, -10f), centro + Offset(0f, -14f), 1.5f)
    drawLine(OroOrbital, centro + Offset(9f, 0f), centro + Offset(12f, 0f), 1.5f)
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
