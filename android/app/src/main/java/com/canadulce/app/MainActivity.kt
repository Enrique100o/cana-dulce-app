package com.canadulce.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.canadulce.app.datos.Pantalla
import com.canadulce.app.ui.AplicacionCanaDulce
import com.canadulce.app.ui.PrincipalViewModel
import com.canadulce.app.ui.theme.CanaDulceTheme

class MainActivity : ComponentActivity() {
    private val modelo by lazy {
        ViewModelProvider(this)[PrincipalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val oscuro = isSystemInDarkTheme() || modelo.estado.sesion?.rol == "ESTUDIANTE" ||
                modelo.estado.pantalla in listOf(Pantalla.INICIO, Pantalla.ESTUDIANTE, Pantalla.REGISTRO)
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !oscuro
                    isAppearanceLightNavigationBars = !oscuro
                }
            }
            CanaDulceTheme(darkTheme = oscuro) {
                AplicacionCanaDulce(modelo)
            }
        }
    }
}
