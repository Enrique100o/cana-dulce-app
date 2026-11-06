package com.canadulce.app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Companero

@Composable
fun ReaccionRespuesta(
    companero: Companero,
    movimiento: Boolean,
    correcta: Boolean,
    titulo: String,
    mensaje: String,
    pulso: Int = 0,
    iniciar: Boolean = true,
) {
    val animar = permiteMovimiento(movimiento)
    var repeticion by remember { mutableIntStateOf(0) }
    val gesto = when {
        !iniciar -> GestoOrbital.FLOTAR
        correcta -> GestoOrbital.CELEBRAR
        else -> GestoOrbital.DESANIMAR
    }
    val color = if (correcta) MentaOrbital else OroOrbital
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF30215D), Color(0xFF171B3E))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EtiquetaOrbital(if (correcta) "¡LO CONSEGUISTE!" else "PODEMOS VOLVER A INTENTARLO", color)
        PersonajeOrbital(
            companero, movimiento,
            Modifier.size(width = 176.dp, height = 194.dp),
            gesto, pulso + repeticion,
        )
        Text(
            titulo,
            color = color,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Text(mensaje, color = Color(0xFFE3DAFF), style = MaterialTheme.typography.bodyLarge)
        if (animar) {
            TextButton(onClick = { repeticion++ }) { Text("Repetir animación", color = color) }
        }
    }
}
