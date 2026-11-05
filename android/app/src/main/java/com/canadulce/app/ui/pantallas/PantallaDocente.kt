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
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaDocente(estado: EstadoApp, modelo: PrincipalViewModel) {
    val gestion = estado.gestion
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Área docente", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = modelo::cerrarSesion) { Text("Cerrar sesión") }
            }
            Text("Crea misiones y acompaña el progreso de tus estudiantes.")
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!gestion.listaEstudiantes, { modelo.cambiarSeccionDocente(false) }, { Text("Lecturas") })
                FilterChip(gestion.listaEstudiantes, { modelo.cambiarSeccionDocente(true) }, { Text("Estudiantes") })
                TextButton(onClick = modelo::refrescar, enabled = !estado.cargando) { Text("Actualizar") }
            }
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
        }
        if (gestion.listaEstudiantes) {
            item {
                OutlinedTextField(
                    gestion.busqueda,
                    { modelo.filtrarEstudiantes(it, gestion.grado, gestion.estadoPerfil) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Buscar por alias") },
                    singleLine = true,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectorDocente(
                        "Grado",
                        gestion.grado?.toString() ?: "Todos",
                        listOf("Todos", "3", "4"),
                        { modelo.filtrarEstudiantes(gestion.busqueda, it.toIntOrNull(), gestion.estadoPerfil) },
                        Modifier.weight(1f),
                    )
                    SelectorDocente(
                        "Estado",
                        gestion.estadoPerfil ?: "Todos",
                        listOf("Todos", "ACTIVO", "INACTIVO"),
                        { modelo.filtrarEstudiantes(gestion.busqueda, gestion.grado, it.takeUnless { s -> s == "Todos" }) },
                        Modifier.weight(1f),
                    )
                }
                if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (estado.estudiantes.isEmpty() && !estado.cargando && estado.error == null) {
                item { Text("No encontramos estudiantes con estos filtros.") }
            }
            items(estado.estudiantes, key = { it.idEstudiante }) { perfil ->
                Card(
                    onClick = { modelo.abrirProgreso(perfil) },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(perfil.alias, style = MaterialTheme.typography.titleMedium)
                        Text("${perfil.grado}.º grado · ${perfil.estado.lowercase()}")
                        Text("Consultar progreso →", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            item {
                BotonPrincipal("Nueva lectura", cargando = estado.cargando, onClick = { modelo.editarLectura(true) })
                OutlinedTextField(
                    estado.busqueda, modelo::buscar, Modifier.fillMaxWidth(),
                    label = { Text("Buscar una lectura") }, singleLine = true,
                )
                if (estado.cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (estado.lecturas.isEmpty() && !estado.cargando && estado.error == null) {
                item { Text("No hay lecturas que coincidan con la búsqueda.") }
            }
            items(estado.lecturas, key = { it.id }) { lectura ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (lectura.estado == "ACTIVA") "PUBLICADA" else "BORRADOR O INACTIVA",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TarjetaMision(lectura, { modelo.abrirLectura(lectura.id) }, estado.cargando)
                }
            }
        }
    }
}
