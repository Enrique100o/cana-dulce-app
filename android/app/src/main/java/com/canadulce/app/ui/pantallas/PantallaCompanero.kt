package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.Companero
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaCompanero(
    estado: EstadoApp,
    onElegir: (Companero) -> Unit,
    onVolver: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TextButton(onClick = onVolver) {
                Text(if (estado.companero == null) "‹ Volver al inicio" else "‹ Mapa estelar")
            }
            EtiquetaOrbital("TRIPULACIÓN")
            Spacer(Modifier.height(8.dp))
            Text("¿Con quién quieres jugar?", style = MaterialTheme.typography.headlineMedium)
            Text("Elige a quien te acompañará por la galaxia. Puedes cambiar después.")
        }
        items(Companero.entries, key = { it.name }) { companero ->
            val lina = companero == Companero.LINA
            val acento = if (lina) OroOrbital else MentaOrbital
            val fondo = if (lina) listOf(Color(0xFF17434D), Color(0xFF1F3549))
            else listOf(Color(0xFF39246A), Color(0xFF25204D))
            Surface(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.background(Brush.horizontalGradient(fondo)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PersonajeOrbital(companero, estado.movimiento, Modifier.size(106.dp))
                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                            EtiquetaOrbital(if (lina) "EXPLORADORA" else "PILOTO", acento)
                            Text(companero.nombre, style = MaterialTheme.typography.headlineSmall)
                            Text(companero.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(
                        onClick = { onElegir(companero) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = acento, contentColor = Color(0xFF18132E)),
                    ) {
                        Text(if (estado.companero == companero) "Seguir con ${companero.nombre}" else "Elegir a ${companero.nombre}")
                    }
                }
            }
        }
    }
}
