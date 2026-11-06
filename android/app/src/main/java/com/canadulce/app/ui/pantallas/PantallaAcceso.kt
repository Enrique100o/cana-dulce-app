package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*

@Composable
fun PantallaAcceso(
    estado: EstadoApp,
    docente: Boolean,
    onVolver: () -> Unit,
    onEntrar: (String, String) -> Unit,
    onRegistro: () -> Unit,
    onCerrarMensaje: () -> Unit,
) {
    var usuario by rememberSaveable { mutableStateOf(if (docente) "" else estado.aliasSugerido) }
    var codigo by remember { mutableStateOf("") }
    val foco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    PaginaFormulario(estado.cargando, onVolver) {
        if (docente) {
            CabeceraDocente(
                "CAÑA DULCE · ÁREA DOCENTE",
                "Tu próxima historia empieza aquí",
                "Organiza tus lecturas y acompaña el progreso de tu grupo.",
            )
        } else {
            EncabezadoAventura(
                "¡Qué alegría verte!",
                "Tu mundo de historias está listo para continuar.",
                "MI AVENTURA",
            )
        }
        MensajePantalla(estado.error, estado.aviso, onCerrarMensaje)
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (docente) "Usuario docente" else "Tu alias") },
            singleLine = true,
            enabled = !estado.cargando,
        )
        CampoCodigo(codigo, { codigo = it }, docente, !estado.cargando)
        BotonPrincipal(if (docente) "Entrar al área docente" else "Entrar", estado.cargando) {
            foco.clearFocus()
            teclado?.hide()
            onEntrar(usuario, codigo)
        }
        if (!docente)
            TextButton(onClick = onRegistro, enabled = !estado.cargando) {
                Text("Quiero crear mi perfil")
            }
    }
}
