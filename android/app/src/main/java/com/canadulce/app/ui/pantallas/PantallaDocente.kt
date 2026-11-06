package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.datos.Lectura
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaDocente(estado: EstadoApp, modelo: PrincipalViewModel) {
    val gestion = estado.gestion
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Área docente",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = modelo::cerrarSesion) { Text("Cerrar sesión") }
            }
            CabeceraDocente(
                "CAÑA DULCE · DOCENTES",
                "Tu aula de lectura",
                "Prepara historias que despierten la curiosidad y acompaña cada avance.",
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (gestion.listaEstudiantes) {
                    IndicadorDocente(
                        if (estado.cargando) "—" else estado.estudiantes.size.toString(),
                        "Estudiantes en esta vista",
                        SimboloDocente.ESTUDIANTES,
                        Modifier.weight(1f),
                    )
                    IndicadorDocente(
                        gestion.grado?.let { "$it.º" } ?: "3.º y 4.º",
                        "Grados seleccionados",
                        SimboloDocente.LIBRO,
                        Modifier.weight(1f),
                    )
                } else {
                    IndicadorDocente(
                        if (estado.cargando) "—" else estado.lecturas.size.toString(),
                        "Lecturas en esta vista",
                        SimboloDocente.LIBRO,
                        Modifier.weight(1f),
                    )
                    IndicadorDocente(
                        if (estado.cargando) "—"
                        else estado.lecturas.count { it.estado == "ACTIVA" }.toString(),
                        "Publicadas en esta vista",
                        SimboloDocente.PREGUNTAS,
                        Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = !gestion.listaEstudiantes,
                    onClick = { modelo.cambiarSeccionDocente(false) },
                    label = { Text("Lecturas") },
                    leadingIcon = { IconoDocente(SimboloDocente.LIBRO) },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                )
                FilterChip(
                    selected = gestion.listaEstudiantes,
                    onClick = { modelo.cambiarSeccionDocente(true) },
                    label = { Text("Estudiantes") },
                    leadingIcon = { IconoDocente(SimboloDocente.ESTUDIANTES) },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                )
            }
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (gestion.listaEstudiantes) "Seguimiento del aula"
                    else "Biblioteca de lecturas",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = modelo::refrescar, enabled = !estado.cargando) {
                    Text("Actualizar")
                }
            }
        }
        if (gestion.listaEstudiantes) {
            item {
                SeccionDocente(
                    "Encuentra un estudiante",
                    "Busca su alias o elige los filtros del grupo.",
                ) {
                    OutlinedTextField(
                        gestion.busqueda,
                        { modelo.filtrarEstudiantes(it, gestion.grado, gestion.estadoPerfil) },
                        Modifier.fillMaxWidth(),
                        label = { Text("Buscar por alias") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SelectorDocente(
                            "Grado",
                            gestion.grado?.toString() ?: "Todos",
                            listOf("Todos", "3", "4"),
                            {
                                modelo.filtrarEstudiantes(
                                    gestion.busqueda,
                                    it.toIntOrNull(),
                                    gestion.estadoPerfil,
                                )
                            },
                            Modifier.weight(1f),
                        )
                        SelectorDocente(
                            "Estado",
                            gestion.estadoPerfil ?: "Todos",
                            listOf("Todos", "ACTIVO", "INACTIVO"),
                            {
                                modelo.filtrarEstudiantes(
                                    gestion.busqueda,
                                    gestion.grado,
                                    it.takeUnless { s -> s == "Todos" },
                                )
                            },
                            Modifier.weight(1f),
                        )
                    }
                }
            }
            if (estado.cargando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (estado.estudiantes.isEmpty() && !estado.cargando && estado.error == null) {
                item {
                    VacioDocente(
                        "El grupo aparecerá aquí",
                        "No encontramos estudiantes con estos filtros.",
                        SimboloDocente.ESTUDIANTES,
                    )
                }
            }
            items(estado.estudiantes, key = { it.idEstudiante }) { perfil ->
                Card(
                    onClick = { modelo.abrirProgreso(perfil) },
                    enabled = !estado.cargando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            IconoDocente(
                                SimboloDocente.ESTUDIANTES,
                                Modifier.padding(14.dp).size(28.dp),
                            )
                        }
                        Column(
                            Modifier.weight(1f).padding(start = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Text(
                                perfil.alias,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "${perfil.grado}.º grado",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            EstadoDocente(
                                perfil.estado.lowercase().replaceFirstChar { it.titlecase() },
                                perfil.estado == "ACTIVO",
                            )
                            Text(
                                "Consultar progreso →",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                SeccionDocente(
                    "Historias para tu grupo",
                    "Crea un borrador y completa su cuestionario antes de publicarlo.",
                ) {
                    BotonPrincipal("Nueva lectura", cargando = estado.cargando) {
                        modelo.editarLectura(true)
                    }
                    OutlinedTextField(
                        estado.busqueda,
                        modelo::buscar,
                        Modifier.fillMaxWidth(),
                        label = { Text("Buscar una lectura") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }
            if (estado.cargando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (estado.lecturas.isEmpty() && !estado.cargando && estado.error == null) {
                item {
                    VacioDocente(
                        "Dale vida a una nueva historia",
                        "No hay lecturas que coincidan con la búsqueda.",
                        SimboloDocente.LIBRO,
                    )
                }
            }
            items(estado.lecturas, key = { it.id }) { lectura ->
                TarjetaLecturaDocente(lectura, estado.cargando) { modelo.abrirLectura(lectura.id) }
            }
        }
    }
}

@Composable
private fun TarjetaLecturaDocente(lectura: Lectura, cargando: Boolean, onAbrir: () -> Unit) {
    val cuestionario = if (lectura.totalPreguntas == 1) "1 pregunta activa"
    else "${lectura.totalPreguntas} preguntas activas"
    Card(
        onClick = onAbrir,
        enabled = !cargando,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconoDocente(SimboloDocente.LIBRO, Modifier.size(28.dp))
                Spacer(Modifier.weight(1f))
                EstadoDocente(
                    if (lectura.estado == "ACTIVA") "Publicada" else "Borrador o inactiva",
                    lectura.estado == "ACTIVA",
                )
            }
            Text(
                lectura.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${lectura.grado}.º grado · Nivel ${lectura.dificultad.lowercase()} · $cuestionario",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (lectura.categoria.isNotBlank())
                Text(lectura.categoria, style = MaterialTheme.typography.labelLarge)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                "Administrar lectura →",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
