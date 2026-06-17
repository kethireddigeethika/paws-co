package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WarmBrown,
    onPrimary = Color.White,
    primaryContainer = LightBeige,
    onPrimaryContainer = DarkBrown,
    secondary = LightBeige,
    onSecondary = DarkBrown,
    background = BaseCream,
    onBackground = CharcoalText,
    surface = Color.White,
    onSurface = CharcoalText,
    surfaceVariant = LightBeige,
    onSurfaceVariant = DarkBrown,
    error = ErrorColor,
    onError = Color.White,
    tertiary = DarkBrown,
    onTertiary = BaseCream
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldenBeige,
    onPrimary = DarkEspresso,
    primaryContainer = MutedBrown,
    onPrimaryContainer = SoftCream,
    secondary = DarkSurface,
    onSecondary = GoldenBeige,
    background = DarkEspresso,
    onBackground = DarkCharcoalText,
    surface = DarkSurface,
    onSurface = SoftCream,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = GoldenBeige,
    error = ErrorColor,
    onError = Color.White,
    tertiary = GoldenBeige,
    onTertiary = DarkEspresso
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
