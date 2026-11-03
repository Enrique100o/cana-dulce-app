package com.canadulce.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresClaros =
    lightColorScheme(
        primary = Bosque,
        onPrimary = Color.White,
        primaryContainer = Hoja,
        onPrimaryContainer = Color(0xFF123E2E),
        secondary = Brisa,
        secondaryContainer = Color(0xFFD9EFEF),
        onSecondaryContainer = Color(0xFF133C42),
        tertiary = Color(0xFF775700),
        tertiaryContainer = Color(0xFFFFE8A8),
        background = Arena,
        onBackground = Color(0xFF20382C),
        surface = Color(0xFFFFFEF9),
        onSurface = Color(0xFF20382C),
        surfaceVariant = Color(0xFFE9EDE1),
        onSurfaceVariant = Color(0xFF4B5D50),
        outline = Color(0xFF788B79),
    )

private val ColoresOscuros =
    darkColorScheme(
        primary = Color(0xFF9EDBB6),
        onPrimary = Color(0xFF053825),
        primaryContainer = Color(0xFF164F38),
        onPrimaryContainer = Hoja,
        secondary = Color(0xFFA8D7D7),
        secondaryContainer = Color(0xFF214A4D),
        tertiary = Sol,
        background = Color(0xFF101D17),
        onBackground = Color(0xFFE0EBDD),
        surface = Color(0xFF17271F),
        onSurface = Color(0xFFE0EBDD),
        surfaceVariant = Color(0xFF304339),
        onSurfaceVariant = Color(0xFFBCD0BD),
    )

@Composable
fun CanaDulceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ColoresOscuros else ColoresClaros,
        typography = Typography,
        content = content,
    )
}
