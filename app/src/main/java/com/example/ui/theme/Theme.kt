package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaLight,
    onPrimary = TerracottaDark,
    primaryContainer = TerracottaPrimary,
    onPrimaryContainer = Color.White,
    secondary = SaffronLight,
    onSecondary = SaffronDark,
    secondaryContainer = SaffronSecondary,
    onSecondaryContainer = Color.White,
    tertiary = AccentGold,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = WarmOutline,
    outlineVariant = Color(0xFF534341)
)

private val LightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = SaffronSecondary,
    onSecondary = Color.White,
    secondaryContainer = SaffronContainer,
    onSecondaryContainer = OnSaffronContainer,
    tertiary = RoyalBronze,
    background = WarmWhiteBackground,
    onBackground = WarmOnSurface,
    surface = WarmWhiteSurface,
    onSurface = WarmOnSurface,
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding palette
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
        content = content
    )
}
