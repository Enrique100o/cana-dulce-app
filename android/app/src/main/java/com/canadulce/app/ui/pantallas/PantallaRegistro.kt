package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*
import com.canadulce.app.ui.theme.CanaDulceTheme

@Composable
fun PantallaRegistro(
    estado: EstadoApp,
    onVolver: () -> Unit,
    onRegistrar: (String, Int?, String) -> Unit,
    onIngresar: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    var alias by rememberSaveable { mutableStateOf("") }
    var grado by rememberSaveable { mutableStateOf<Int?>(null) }
    var codigo by remember { mutableStateOf("") }
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current

    PaginaFormulario(estado.cargando, onVolver) {
        EncabezadoAventura(
            "Crea tu perfil de lector",
            "Elige un apodo y empieza tu aventura entre historias.",
            "MI PRIMERA SEMILLA",
        )
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        OutlinedTextField(
            value = alias,
            onValueChange = { alias = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !estado.cargando,
            label = { Text("Tu alias") },
            placeholder = { Text("Ejemplo: LectorValiente") },
            supportingText = { Text("Un apodo de 3 a 30 caracteres.") },
            singleLine = true,
        )
        Text("¿En qué grado estás?", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(3 to "Tercero", 4 to "Cuarto").forEach { (numero, nombre) ->
                FilterChip(
                    selected = grado == numero,
                    onClick = { grado = numero },
                    enabled = !estado.cargando,
                    modifier = Modifier.weight(1f).heightIn(min = 62.dp),
                    label = { Text(nombre, style = MaterialTheme.typography.titleMedium) },
                )
            }
        }
        CampoCodigo(codigo, { codigo = it }, habilitado = !estado.cargando)
        Text(
            "Elige un código de 6 a 32 caracteres. Lo usarás para volver a entrar.",
            style = MaterialTheme.typography.bodyMedium,
        )
        BotonPrincipal("Crear mi perfil", estado.cargando) {
            foco.clearFocus()
            teclado?.hide()
            onRegistrar(alias, grado, codigo)
        }
        TextButton(onClick = onIngresar, enabled = !estado.cargando) { Text("Ya tengo un perfil") }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPreviaRegistro() {
    CanaDulceTheme {
        PantallaRegistro(EstadoApp(), {}, { _, _, _ -> }, {}, {})
    }
}
