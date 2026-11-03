package com.canadulce.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
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
            CanaDulceTheme {
                AplicacionCanaDulce(modelo)
            }
        }
    }
}
