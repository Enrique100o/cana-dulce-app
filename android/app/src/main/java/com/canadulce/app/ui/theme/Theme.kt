package com.canadulce.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresClaros =
    lightColorScheme(
        primary = Color(0xFF5841B7),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE8DFFF),
        onPrimaryContainer = Color(0xFF291457),
        secondary = Brisa,
        secondaryContainer = Color(0xFFD9EFEF),
        onSecondaryContainer = Color(0xFF133C42),
        tertiary = Color(0xFF775700),
        tertiaryContainer = Color(0xFFFFE8A8),
        background = Color(0xFFF9F7FF),
        onBackground = Color(0xFF20382C),
        surface = Color(0xFFFFFEF9),
        onSurface = Color(0xFF20382C),
        surfaceVariant = Color(0xFFE9EDE1),
        onSurfaceVariant = Color(0xFF4B5D50),
        outline = Color(0xFF788B79),
    )

private val ColoresOscuros =
    darkColorScheme(
        primary = Color(0xFFC7B9FF),
        onPrimary = Color(0xFF23104D),
        primaryContainer = Color(0xFF3C296D),
        onPrimaryContainer = Color(0xFFF0E7FF),
        secondary = Color(0xFF9FFBE6),
        secondaryContainer = Color(0xFF214A55),
        onSecondaryContainer = Color(0xFFD0FFF5),
        tertiary = Color(0xFFFFDC80),
        background = Color(0xFF100D23),
        onBackground = Color(0xFFF5F0FF),
        surface = Color(0xFF1D1837),
        surfaceContainer = Color(0xFF211A3D),
        surfaceContainerHigh = Color(0xFF291F47),
        surfaceContainerHighest = Color(0xFF302450),
        onSurface = Color(0xFFF5F0FF),
        surfaceVariant = Color(0xFF2B2249),
        onSurfaceVariant = Color(0xFFD7CDEC),
        outline = Color(0xFF8B7FAA),
        outlineVariant = Color(0xFF4B3F6C),
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
