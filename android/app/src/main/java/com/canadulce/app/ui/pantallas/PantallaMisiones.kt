package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaMisiones(
    estado: EstadoApp,
    onBuscar: (String) -> Unit,
    onAbrir: (Int) -> Unit,
    onRefrescar: () -> Unit,
    onSalir: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Mi aventura", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onSalir) { Text("Cerrar sesión") }
        }
        EncabezadoAventura(
            "Hola, " + estado.sesion?.estudiante?.alias.orEmpty(),
            "Elige una historia de tu grado y deja volar tu imaginación.",
            "EXPLORADORES DE HISTORIAS",
        )
        OutlinedTextField(
            value = estado.busqueda,
            onValueChange = onBuscar,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar una misión") },
            singleLine = true,
            trailingIcon = {
                if (estado.busqueda.isNotEmpty())
                    TextButton(onClick = { onBuscar("") }) { Text("Limpiar") }
            },
        )
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Misiones disponibles: " + estado.lecturas.size,
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(onClick = onRefrescar, enabled = !estado.cargando) { Text("Actualizar") }
        }
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (estado.lecturas.isEmpty() && !estado.cargando)
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "Hay historias por descubrir",
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                if (estado.busqueda.isBlank())
                                    "Aún no hay misiones disponibles para tu grado. Vuelve a mirar más tarde."
                                else
                                    "No encontramos ese título. Prueba con otra palabra o limpia la búsqueda.",
                            )
                        }
                    }
                }
            items(estado.lecturas, key = { it.id }) { lectura ->
                TarjetaMision(lectura, { onAbrir(lectura.id) }, estado.cargando)
            }
        }
    }
}
