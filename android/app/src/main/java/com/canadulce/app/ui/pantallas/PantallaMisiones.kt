package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Companero
import com.canadulce.app.datos.Intento
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaMisiones(
    estado: EstadoApp,
    onBuscar: (String) -> Unit,
    onAbrir: (Int) -> Unit,
    onRetomar: (Intento) -> Unit,
    onProgreso: () -> Unit,
    onMovimiento: () -> Unit,
    onRefrescar: () -> Unit,
    onSalir: () -> Unit,
    onCerrarMensaje: () -> Unit,
    onCompanero: () -> Unit,
    onEntrenar: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Mi aventura", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onSalir) { Text("Cerrar sesión") }
            }
        }
        item {
            HeroeOrbital(
                "MAPA ESTELAR",
                "Hola, " + estado.sesion?.estudiante?.alias.orEmpty(),
                "${estado.companero?.nombre ?: "Astro"} te acompaña. Elige un planeta y descubre su historia.",
                estado.movimiento,
                companero = estado.companero ?: Companero.ASTRO,
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCompanero, enabled = !estado.cargando, modifier = Modifier.weight(1f)) {
                    Text("Cambiar personaje")
                }
                OutlinedButton(onClick = onEntrenar, enabled = !estado.cargando, modifier = Modifier.weight(1f)) {
                    Text("Entrenamiento")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onProgreso, modifier = Modifier.weight(1f)) {
                    Text("Mi progreso")
                    estado.progreso?.let { Text("  ·  ${it.estrellas} ★") }
                }
                OutlinedButton(onClick = onMovimiento, modifier = Modifier.weight(1f)) {
                    Text(if (estado.movimiento) "Pausar movimiento" else "Activar movimiento")
                }
            }
        }
        items(estado.pendientes, key = { "pendiente-${it.id}" }) { intento ->
            Card(
                onClick = { onRetomar(intento) },
                enabled = !estado.cargando,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EtiquetaOrbital("MISIÓN EN CURSO")
                    Text(intento.titulo, style = MaterialTheme.typography.titleMedium)
                    Text("Retomar misión →", fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = onBuscar,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar una misión") },
                singleLine = true,
                trailingIcon = {
                    if (estado.busqueda.isNotEmpty()) TextButton(onClick = { onBuscar("") }) { Text("Limpiar") }
                },
            )
            MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Misiones disponibles: ${estado.lecturas.size}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                )
                TextButton(onClick = onRefrescar, enabled = !estado.cargando) { Text("Actualizar") }
            }
            if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        if (estado.lecturas.isEmpty() && !estado.cargando && estado.error == null) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Hay historias por descubrir", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (estado.busqueda.isBlank()) "Aún no hay misiones disponibles para tu grado."
                            else "No encontramos ese título. Prueba con otra palabra o limpia la búsqueda.",
                        )
                    }
                }
            }
        }
        items(estado.lecturas, key = { it.id }) { lectura ->
            TarjetaMision(lectura, { onAbrir(lectura.id) }, estado.cargando, espacial = true)
        }
    }
}
