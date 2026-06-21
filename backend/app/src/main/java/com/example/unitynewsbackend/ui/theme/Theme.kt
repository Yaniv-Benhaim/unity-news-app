package com.example.unitynewsbackend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ConsoleDarkPrimary,
    onPrimary = ConsoleDarkPaper,
    primaryContainer = ConsoleDarkPrimarySoft,
    onPrimaryContainer = ConsoleDarkInk,
    secondary = ConsoleDarkMuted,
    onSecondary = ConsoleDarkPaper,
    secondaryContainer = ConsoleDarkBorder,
    onSecondaryContainer = ConsoleDarkInk,
    background = ConsoleDarkPaper,
    onBackground = ConsoleDarkInk,
    surface = ConsoleDarkSurface,
    onSurface = ConsoleDarkInk,
    surfaceVariant = ConsoleDarkBorder,
    onSurfaceVariant = ConsoleDarkMuted,
    outline = ConsoleDarkBorder,
)

private val LightColorScheme = lightColorScheme(
    primary = ConsolePrimary,
    onPrimary = ConsoleSurface,
    primaryContainer = ConsolePrimarySoft,
    onPrimaryContainer = ConsoleInk,
    secondary = ConsoleInk,
    onSecondary = ConsoleSurface,
    secondaryContainer = ConsoleBorder,
    onSecondaryContainer = ConsoleInk,
    background = ConsolePaper,
    onBackground = ConsoleInk,
    surface = ConsoleSurface,
    onSurface = ConsoleInk,
    surfaceVariant = ConsoleBorder,
    onSurfaceVariant = ConsoleMuted,
    outline = ConsoleBorder,
)

@Composable
fun UnityNewsBackendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
