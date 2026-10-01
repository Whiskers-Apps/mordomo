package org.whiskersapps.mordomo.ui.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import lib.Settings


fun String.toColor(): Color {
    return Color(this.toColorInt())
}

data class ThemeColors(
    val main: Color = Color.Unspecified,
    val secondary: Color = Color.Unspecified,
    val tertiary: Color = Color.Unspecified,
    val textMain: Color = Color.Unspecified,
    val textSecondary: Color = Color.Unspecified,
    val textDisabled: Color = Color.Unspecified,
    val accent: Color = Color.Unspecified,
    val onAccent: Color = Color.Unspecified,
    val danger: Color = Color.Unspecified,
    val onDanger: Color = Color.Unspecified,
)

val LocalTheme = staticCompositionLocalOf { ThemeColors() }

@Composable
fun MordomoTheme(
    settings: Settings,
    content: @Composable () -> Unit
) {

    val colorScheme = remember(settings) {
        if (settings.theme.dark) {
            darkColorScheme(
                primary = settings.theme.accent.toColor(),
                onPrimary = settings.theme.onAccent.toColor(),
                primaryContainer = settings.theme.accent.toColor(),
                onPrimaryContainer = settings.theme.onAccent.toColor(),

                inversePrimary = settings.theme.accent.toColor(),

                secondary = settings.theme.secondary.toColor(),
                onSecondary = settings.theme.textMain.toColor(),
                secondaryContainer = settings.theme.secondary.toColor(),
                onSecondaryContainer = settings.theme.textMain.toColor(),

                tertiary = settings.theme.tertiary.toColor(),
                onTertiary = settings.theme.textMain.toColor(),
                tertiaryContainer = settings.theme.tertiary.toColor(),
                onTertiaryContainer = settings.theme.textMain.toColor(),

                error = settings.theme.danger.toColor(),
                onError = settings.theme.onDanger.toColor(),
                errorContainer = settings.theme.danger.toColor(),
                onErrorContainer = settings.theme.onDanger.toColor(),

                background = settings.theme.main.toColor(),
                onBackground = settings.theme.textMain.toColor(),

                surface = settings.theme.main.toColor(),
                surfaceTint = settings.theme.main.toColor(),
                surfaceBright = settings.theme.main.toColor(),
                surfaceDim = settings.theme.tertiary.toColor(),
                surfaceContainerLowest = settings.theme.main.toColor(),
                surfaceContainerLow = settings.theme.main.toColor(),
                surfaceContainer = settings.theme.main.toColor(),
                surfaceContainerHigh = settings.theme.secondary.toColor(),
                surfaceContainerHighest = settings.theme.tertiary.toColor(),
                onSurface = settings.theme.textMain.toColor(),

                inverseSurface = settings.theme.textMain.toColor(),
                inverseOnSurface = settings.theme.main.toColor(),

                surfaceVariant = settings.theme.secondary.toColor(),
                onSurfaceVariant = settings.theme.textMain.toColor(),

                outline = settings.theme.secondary.toColor(),
                outlineVariant = settings.theme.tertiary.toColor(),

                scrim = Color.Black,
            )
        } else {
            lightColorScheme(
                primary = settings.theme.accent.toColor(),
                onPrimary = settings.theme.onAccent.toColor(),
                primaryContainer = settings.theme.accent.toColor(),
                onPrimaryContainer = settings.theme.onAccent.toColor(),

                inversePrimary = settings.theme.accent.toColor(),

                secondary = settings.theme.secondary.toColor(),
                onSecondary = settings.theme.textMain.toColor(),
                secondaryContainer = settings.theme.secondary.toColor(),
                onSecondaryContainer = settings.theme.textMain.toColor(),

                tertiary = settings.theme.tertiary.toColor(),
                onTertiary = settings.theme.textMain.toColor(),
                tertiaryContainer = settings.theme.tertiary.toColor(),
                onTertiaryContainer = settings.theme.textMain.toColor(),

                error = settings.theme.danger.toColor(),
                onError = settings.theme.onDanger.toColor(),
                errorContainer = settings.theme.danger.toColor(),
                onErrorContainer = settings.theme.onDanger.toColor(),

                background = settings.theme.main.toColor(),
                onBackground = settings.theme.textMain.toColor(),

                surface = settings.theme.main.toColor(),
                surfaceTint = settings.theme.main.toColor(),
                surfaceBright = settings.theme.main.toColor(),
                surfaceDim = settings.theme.tertiary.toColor(),
                surfaceContainerLowest = settings.theme.main.toColor(),
                surfaceContainerLow = settings.theme.main.toColor(),
                surfaceContainer = settings.theme.main.toColor(),
                surfaceContainerHigh = settings.theme.secondary.toColor(),
                surfaceContainerHighest = settings.theme.tertiary.toColor(),
                onSurface = settings.theme.textMain.toColor(),

                inverseSurface = settings.theme.textMain.toColor(),
                inverseOnSurface = settings.theme.main.toColor(),

                surfaceVariant = settings.theme.secondary.toColor(),
                onSurfaceVariant = settings.theme.textMain.toColor(),

                outline = settings.theme.secondary.toColor(),
                outlineVariant = settings.theme.tertiary.toColor(),

                scrim = Color.Black,
            )
        }
    }

    val themeColors = remember(settings.theme) {
        ThemeColors(
            main = settings.theme.main.toColor(),
            secondary = settings.theme.secondary.toColor(),
            tertiary = settings.theme.tertiary.toColor(),
            textMain = settings.theme.textMain.toColor(),
            textSecondary = settings.theme.textSecondary.toColor(),
            textDisabled = settings.theme.textDisabled.toColor(),
            accent = settings.theme.accent.toColor(),
            onAccent = settings.theme.onAccent.toColor(),
            danger = settings.theme.danger.toColor(),
            onDanger = settings.theme.onDanger.toColor()
        )
    }

    CompositionLocalProvider(LocalTheme provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}