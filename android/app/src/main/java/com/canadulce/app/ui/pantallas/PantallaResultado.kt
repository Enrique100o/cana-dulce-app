package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.BotonPrincipal
import com.canadulce.app.ui.componentes.EncabezadoAventura
import com.canadulce.app.ui.componentes.PaginaFormulario
import java.util.Locale

@Composable
fun PantallaResultado(estado: EstadoApp, onVolver: () -> Unit) {
    val resultado = estado.resultado ?: return
    PaginaFormulario(cargando = false, onVolver = onVolver) {
        EncabezadoAventura(
            "¡Misión completada!",
            "Cada historia te ayuda a crecer. Sigue descubriendo nuevas aventuras.",
            "TU ESFUERZO CUENTA",
        )
        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "★".repeat(resultado.estrellas) + "☆".repeat(3 - resultado.estrellas),
                    modifier =
                        Modifier.semantics {
                            contentDescription = "${resultado.estrellas} estrellas"
                        },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    String.format(Locale.forLanguageTag("es-CO"), "%.2f puntos", resultado.puntaje),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text("${resultado.correctas} de ${resultado.total} respuestas correctas")
                Text(
                    if (resultado.puntaje >= 80) "¡Excelente lectura!"
                    else "Vuelve a leer con calma: cada intento es una oportunidad para aprender.",
                )
            }
        }
        BotonPrincipal("Volver a misiones", onClick = onVolver)
    }
}
