package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaInicio(
    movimiento: Boolean,
    onMovimiento: () -> Unit,
    onIngresar: () -> Unit,
    onRegistrar: () -> Unit,
    onDocente: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("CAÑA DULCE", Modifier.weight(1f), fontWeight = FontWeight.ExtraBold)
                TextButton(onClick = onMovimiento) {
                    Text(if (movimiento) "Pausar movimiento" else "Activar movimiento")
                }
            }
        }
        item {
            HeroeOrbital(
                "ÓRBITA LECTORA",
                "Lee y despega",
                "Soy Astro. Explora historias, resuelve retos y llena tu bitácora de estrellas.",
                movimiento,
                grande = true,
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tu aventura comienza aquí", style = MaterialTheme.typography.titleLarge)
                    Text("Misiones de lectura para tercero y cuarto.")
                    BotonPrincipal("Entrar como estudiante", onClick = onIngresar)
                    OutlinedButton(
                        onClick = onRegistrar,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    ) { Text("Crear mi perfil") }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Lee" to "Historia", "Resuelve" to "Reto", "Colecciona" to "Estrellas")
                    .forEachIndexed { indice, (titulo, descripcion) ->
                        Card(Modifier.weight(1f)) {
                            Column(
                                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                PlanetaMision(indice, Modifier.size(49.dp))
                                Text(titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(descripcion, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("CENTRO DE MANDO", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = onDocente) { Text("Acceso docente") }
            }
        }
    }
}
