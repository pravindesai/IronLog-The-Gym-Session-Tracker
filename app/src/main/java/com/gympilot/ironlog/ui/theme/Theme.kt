package com.gympilot.ironlog.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IronPrimary = Color(0xFF618764)
val IronSecondary = Color(0xFF9CB080)
val IronDarkAccent = Color(0xFF2B5748)
val IronText = Color(0xFF273338)
val IronBackground = Color(0xFFF8F9F7)
val IronSurface = Color(0xFFFFFFFF)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = IronPrimary,
    onPrimary = Color.White,
    secondary = IronSecondary,
    onSecondary = IronText,
    tertiary = IronDarkAccent,
    background = IronBackground,
    onBackground = IronText,
    surface = IronSurface,
    onSurface = IronText,
    surfaceVariant = Color(0xFFEAF0E8),
    onSurfaceVariant = Color(0xFF53625A),
    outline = Color(0xFFC8D2C8)
)

@Composable
fun IronLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
