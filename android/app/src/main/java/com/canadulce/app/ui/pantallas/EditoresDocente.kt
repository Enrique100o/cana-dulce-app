package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EdicionLectura
import com.canadulce.app.datos.EdicionPregunta
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.componentes.*

@Composable
internal fun SelectorDocente(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onElegir: (String) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    var abierto by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { abierto = true },
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("$etiqueta: ${valor.replace('_', ' ').lowercase()} ▾")
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion.replace('_', ' ')) },
                    onClick = {
                        abierto = false
                        onElegir(opcion)
                    },
                )
            }
        }
    }
}

@Composable
fun PantallaEditarLectura(estado: EstadoApp, modelo: PrincipalViewModel) {
    val lectura = estado.lectura
    var titulo by rememberSaveable(lectura?.id) { mutableStateOf(lectura?.titulo.orEmpty()) }
    var contenido by rememberSaveable(lectura?.id) { mutableStateOf(lectura?.contenido.orEmpty()) }
    var grado by rememberSaveable(lectura?.id) { mutableIntStateOf(lectura?.grado ?: 3) }
    var dificultad by
        rememberSaveable(lectura?.id) { mutableStateOf(lectura?.dificultad ?: "BAJA") }
    var categoria by rememberSaveable(lectura?.id) { mutableStateOf(lectura?.categoria.orEmpty()) }
    val habilitado = !estado.cargando
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = modelo::volver, enabled = habilitado) { Text("‹ Volver") }
            CabeceraDocente(
                "BIBLIOTECA · EDITOR",
                if (lectura == null) "Nueva lectura" else "Editar lectura",
                "Prepara una historia y después crea los retos para tu grupo.",
            )
        }
        item {
            SeccionDocente(
                "1. Dale identidad",
                "El título y la categoría ayudan a encontrar la lectura.",
            ) {
                OutlinedTextField(
                    titulo,
                    { titulo = it },
                    Modifier.fillMaxWidth(),
                    enabled = habilitado,
                    label = { Text("Título") },
                    singleLine = true,
                    supportingText = {
                        Text("${titulo.codePointCount(0, titulo.length)} de 120 caracteres")
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    categoria,
                    { categoria = it },
                    Modifier.fillMaxWidth(),
                    enabled = habilitado,
                    label = { Text("Categoría (opcional)") },
                    singleLine = true,
                    placeholder = { Text("Por ejemplo, naturaleza o aventuras") },
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
        item {
            SeccionDocente(
                "2. Cuenta la historia",
                "Escribe entre 20 y 8000 caracteres. El estudiante podrá ajustar el tamaño al leer.",
            ) {
                OutlinedTextField(
                    contenido,
                    { contenido = it },
                    Modifier.fillMaxWidth(),
                    enabled = habilitado,
                    label = { Text("Texto de la lectura") },
                    minLines = 7,
                    maxLines = 16,
                    supportingText = {
                        Text("${contenido.codePointCount(0, contenido.length)} de 8000 caracteres")
                    },
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
        item {
            SeccionDocente("3. Elige el grupo", "Asigna el grado y la dificultad de la misión.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectorDocente(
                        "Grado",
                        grado.toString(),
                        listOf("3", "4"),
                        { grado = it.toInt() },
                        Modifier.weight(1f),
                        habilitado,
                    )
                    SelectorDocente(
                        "Nivel",
                        dificultad,
                        listOf("BAJA", "MEDIA", "ALTA"),
                        { dificultad = it },
                        Modifier.weight(1f),
                        habilitado,
                    )
                }
            }
        }
        item {
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
            if (lectura == null) {
                Text(
                    "Se guardará como borrador. Podrás agregar las preguntas antes de publicarla.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
            }
            BotonPrincipal("Guardar lectura", cargando = estado.cargando) {
                modelo.guardarLectura(
                    EdicionLectura(titulo, contenido, grado, dificultad, categoria),
                )
            }
        }
    }
}

@Composable
fun PantallaEditarPregunta(estado: EstadoApp, modelo: PrincipalViewModel) {
    val original = estado.gestion.edicionPregunta
    val ordenes = estado.gestion.preguntas.map { it.orden }.toSet()
    var enunciado by
        rememberSaveable(original?.id) { mutableStateOf(original?.enunciado.orEmpty()) }
    var tipo by rememberSaveable(original?.id) { mutableStateOf(original?.tipo ?: "LITERAL") }
    var orden by
        rememberSaveable(original?.id) {
            mutableStateOf(
                (original?.orden ?: (1..999).firstOrNull { it !in ordenes } ?: 999).toString(),
            )
        }
    var opciones by
        rememberSaveable(original?.id) {
            mutableStateOf(original?.opciones?.map { it.texto } ?: listOf("", ""))
        }
    var correcta by
        rememberSaveable(original?.id) {
            mutableIntStateOf(original?.opciones?.indexOfFirst { it.correcta } ?: -1)
        }
    var activa by rememberSaveable(original?.id) { mutableStateOf(original?.estado != "INACTIVA") }
    val habilitado = !estado.cargando
    LazyColumn(
        Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = modelo::volver, enabled = habilitado) { Text("‹ Volver") }
            CabeceraDocente(
                "CUESTIONARIO · EDITOR",
                if (original == null) "Nueva pregunta" else "Editar pregunta",
                "Crea un reto claro y señala una única respuesta correcta.",
            )
        }
        item {
            SeccionDocente(
                "1. Plantea el reto",
                "Escribe qué debe encontrar o deducir el estudiante en la historia.",
            ) {
                OutlinedTextField(
                    enunciado,
                    { enunciado = it },
                    Modifier.fillMaxWidth(),
                    enabled = habilitado,
                    label = { Text("Enunciado") },
                    minLines = 3,
                    supportingText = {
                        Text("${enunciado.codePointCount(0, enunciado.length)} de 400 caracteres")
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                SelectorDocente(
                    "Tipo",
                    tipo,
                    listOf("LITERAL", "INFERENCIAL", "IDEA_PRINCIPAL", "VOCABULARIO", "SECUENCIA"),
                    { tipo = it },
                    Modifier.fillMaxWidth(),
                    habilitado,
                )
                OutlinedTextField(
                    orden,
                    { orden = it.filter(Char::isDigit).take(3) },
                    Modifier.fillMaxWidth(),
                    enabled = habilitado,
                    label = { Text("Orden") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text("Posición de la pregunta en el cuestionario") },
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
        item {
            Text("2. Prepara las respuestas", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Escribe de dos a cuatro opciones y marca la correcta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(opciones.size) { indice ->
            val seleccionada = correcta == indice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color =
                    if (seleccionada) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer,
                border =
                    BorderStroke(
                        1.dp,
                        if (seleccionada) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = seleccionada,
                            enabled = habilitado,
                            onClick = { correcta = indice },
                            modifier =
                                Modifier.semantics {
                                    contentDescription =
                                        "Marcar opción ${'A' + indice} como correcta"
                                },
                        )
                        Text(
                            if (seleccionada) "Respuesta correcta" else "Marcar como correcta",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    OutlinedTextField(
                        opciones[indice],
                        { texto ->
                            opciones = opciones.mapIndexed { i, anterior ->
                                if (i == indice) texto else anterior
                            }
                        },
                        Modifier.fillMaxWidth(),
                        enabled = habilitado,
                        label = { Text("Opción ${'A' + indice}") },
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (opciones.size < 4) {
                    OutlinedButton(onClick = { opciones = opciones + "" }, enabled = habilitado) {
                        Text("Añadir opción")
                    }
                }
                if (opciones.size > 2) {
                    TextButton(
                        onClick = {
                            opciones = opciones.dropLast(1)
                            if (correcta >= opciones.size) correcta = -1
                        },
                        enabled = habilitado,
                    ) {
                        Text("Quitar última")
                    }
                }
            }
            if (original != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Pregunta activa", Modifier.weight(1f))
                    Switch(
                        checked = activa,
                        enabled = habilitado,
                        onCheckedChange = { activa = it },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            MensajePantalla(estado.error, estado.aviso, modelo::quitarMensaje)
            BotonPrincipal("Guardar pregunta", cargando = estado.cargando) {
                modelo.guardarPregunta(
                    EdicionPregunta(
                        enunciado,
                        tipo,
                        orden.toIntOrNull() ?: 0,
                        opciones,
                        correcta,
                        if (activa) "ACTIVA" else "INACTIVA",
                    ),
                )
            }
        }
    }
}
