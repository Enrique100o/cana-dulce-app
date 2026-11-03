package com.canadulce.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.canadulce.app.datos.EstadoApp
import com.canadulce.app.ui.componentes.*

@Composable
fun CampoCodigo(
    valor: String,
    onCambio: (String) -> Unit,
    docente: Boolean = false,
    habilitado: Boolean = true,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(if (docente) "Contraseña" else "Código de acceso") },
        singleLine = true,
        enabled = habilitado,
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") }
        },
    )
}

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
        EncabezadoAventura(
            if (docente) "Un espacio para acompañar" else "¡Qué alegría verte!",
            if (docente) "Ingresa a tu área de lecturas y estudiantes."
            else "Tu mundo de historias está listo para continuar.",
            if (docente) "ÁREA DOCENTE" else "MI AVENTURA",
        )
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
