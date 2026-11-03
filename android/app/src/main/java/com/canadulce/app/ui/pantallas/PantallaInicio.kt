package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaInicio(onIngresar: () -> Unit, onRegistrar: () -> Unit, onDocente: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        EncabezadoAventura(
            "Pequeños lectores, grandes aventuras",
            "Descubre historias, imagina nuevos mundos y aprende a tu ritmo.",
        )
        Text(
            "¡Tu próxima historia te espera!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Mi rincón de lectura", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Para exploradores de tercero y cuarto.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                BotonPrincipal("Entrar como estudiante", onClick = onIngresar)
                OutlinedButton(
                    onClick = onRegistrar,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                ) {
                    Text("Crear mi perfil")
                }
            }
        }
        Text(
            "Una semilla de imaginación en cada lectura.",
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(onClick = onDocente) { Text("Acceso docente") }
        Spacer(Modifier.height(12.dp))
    }
}
