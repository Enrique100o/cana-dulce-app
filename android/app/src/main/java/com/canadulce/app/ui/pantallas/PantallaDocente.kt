package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.EncabezadoAventura
import com.canadulce.app.ui.componentes.MensajePantalla
import com.canadulce.app.ui.componentes.TarjetaMision

@Composable
fun PantallaDocente(
    estado: EstadoApp,
    onBuscar: (String) -> Unit,
    onAbrir: (Int) -> Unit,
    onRefrescar: () -> Unit,
    onSalir: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    var estudiantes by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Área docente", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onSalir) { Text("Cerrar sesión") }
        }
        EncabezadoAventura(
            "Acompañamos cada lectura",
            "Consulta las historias y los perfiles de tus estudiantes.",
            "JUNTOS APRENDEMOS",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = !estudiantes,
                onClick = { estudiantes = false },
                label = { Text("Lecturas") },
            )
            FilterChip(
                selected = estudiantes,
                onClick = { estudiantes = true },
                label = { Text("Estudiantes") },
            )
            TextButton(onClick = onRefrescar, enabled = !estado.cargando) { Text("Actualizar") }
        }
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        if (!estudiantes)
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = onBuscar,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar una lectura") },
                singleLine = true,
            )
        if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (estudiantes) {
                if (estado.estudiantes.isEmpty())
                    item { Text("Todavía no hay estudiantes registrados.") }
                items(estado.estudiantes, key = { it.idEstudiante }) { perfil ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text(perfil.alias, style = MaterialTheme.typography.titleMedium)
                            Text(perfil.grado.toString() + ".º grado")
                        }
                    }
                }
            } else {
                if (estado.lecturas.isEmpty() && !estado.cargando)
                    item { Text("No hay lecturas que coincidan con la búsqueda.") }
                items(estado.lecturas, key = { it.id }) { lectura ->
                    TarjetaMision(lectura, { onAbrir(lectura.id) }, estado.cargando)
                }
            }
        }
    }
}
