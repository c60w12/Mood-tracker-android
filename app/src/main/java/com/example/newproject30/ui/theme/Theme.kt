package com.example.newproject30.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SageGreen40,
    onPrimary = WarmCreamSurfaceContainerLowest,
    primaryContainer = SageGreen80,
    onPrimaryContainer = SageGreen10,
    secondary = Eucalyptus40,
    onSecondary = WarmCreamSurfaceContainerLowest,
    secondaryContainer = Eucalyptus80,
    onSecondaryContainer = Eucalyptus10,
    tertiary = WarmSand40,
    onTertiary = WarmCreamSurfaceContainerLowest,
    tertiaryContainer = WarmSand80,
    onTertiaryContainer = WarmSand10,
    error = ErrorRed40,
    onError = WarmCreamSurfaceContainerLowest,
    errorContainer = ErrorRed80,
    onErrorContainer = ErrorRed10,
    background = WarmCreamBackground,
    onBackground = TextCharcoal,
    surface = WarmCreamBackground,
    onSurface = TextCharcoal,
    surfaceVariant = SageSurfaceVariantLight,
    onSurfaceVariant = TextOnSurfaceVariantLight,
    outline = SageOutlineLight,
    outlineVariant = SageOutlineVariantLight,
    inverseSurface = TextCharcoal,
    inverseOnSurface = WarmCreamBackground,
    inversePrimary = SageGreen80,
    surfaceContainerLowest = WarmCreamSurfaceContainerLowest,
    surfaceContainerLow = WarmCreamSurfaceContainerLow,
    surfaceContainer = WarmCreamSurfaceContainer,
    surfaceContainerHigh = WarmCreamSurfaceContainerHigh,
    surfaceContainerHighest = WarmCreamSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen80,
    onPrimary = SageGreen30,
    primaryContainer = SageGreen30,
    onPrimaryContainer = SageGreen80,
    secondary = Eucalyptus80,
    onSecondary = Eucalyptus30,
    secondaryContainer = Eucalyptus30,
    onSecondaryContainer = Eucalyptus80,
    tertiary = WarmSand80,
    onTertiary = WarmSand30,
    tertiaryContainer = WarmSand30,
    onTertiaryContainer = WarmSand80,
    error = ErrorRed80,
    onError = ErrorRed30,
    errorContainer = ErrorRed30,
    onErrorContainer = ErrorRed80,
    background = CharcoalBackground,
    onBackground = TextLightWarm,
    surface = CharcoalBackground,
    onSurface = TextLightWarm,
    surfaceVariant = SageSurfaceVariantDark,
    onSurfaceVariant = TextOnSurfaceVariantDark,
    outline = SageOutlineDark,
    outlineVariant = SageOutlineVariantDark,
    inverseSurface = TextLightWarm,
    inverseOnSurface = CharcoalBackground,
    inversePrimary = SageGreen40,
    surfaceContainerLowest = CharcoalSurfaceContainerLowest,
    surfaceContainerLow = CharcoalSurfaceContainerLow,
    surfaceContainer = CharcoalSurfaceContainer,
    surfaceContainerHigh = CharcoalSurfaceContainerHigh,
    surfaceContainerHighest = CharcoalSurfaceContainerHighest
)

@Composable
fun AppTheme(
    isDarkMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkMode -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun NewProject30Theme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    AppTheme(
        isDarkMode = isDarkMode,
        dynamicColor = dynamicColor,
        content = content
    )
}
