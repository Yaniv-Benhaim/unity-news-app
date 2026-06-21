package com.example.unitynewsapp.ui.theme

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
    primary = EditorialDarkAccent,
    onPrimary = EditorialDarkPaper,
    primaryContainer = EditorialDarkAccentSoft,
    onPrimaryContainer = EditorialDarkInk,
    secondary = EditorialDarkMuted,
    onSecondary = EditorialDarkPaper,
    secondaryContainer = EditorialDarkBorder,
    onSecondaryContainer = EditorialDarkInk,
    background = EditorialDarkPaper,
    onBackground = EditorialDarkInk,
    surface = EditorialDarkSurface,
    onSurface = EditorialDarkInk,
    surfaceVariant = EditorialDarkBorder,
    onSurfaceVariant = EditorialDarkMuted,
    outline = EditorialDarkBorder,
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialInk,
    onPrimary = EditorialSurface,
    primaryContainer = EditorialAccentSoft,
    onPrimaryContainer = EditorialInk,
    secondary = EditorialAccent,
    onSecondary = EditorialSurface,
    secondaryContainer = EditorialAccentSoft,
    onSecondaryContainer = EditorialInk,
    background = EditorialPaper,
    onBackground = EditorialInk,
    surface = EditorialSurface,
    onSurface = EditorialInk,
    surfaceVariant = EditorialBorder,
    onSurfaceVariant = EditorialMuted,
    outline = EditorialBorder,
)

@Composable
fun UnityNewsAppTheme(
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
