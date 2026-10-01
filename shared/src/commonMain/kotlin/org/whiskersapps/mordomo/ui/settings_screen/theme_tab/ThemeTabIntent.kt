package org.whiskersapps.mordomo.ui.settings_screen.theme_tab

sealed interface ThemeTabIntent {
    data class DarkModeCheck(val dark: Boolean) : ThemeTabIntent
    data class MainPick(val hex: String): ThemeTabIntent
    data class SecondaryPick(val hex: String): ThemeTabIntent
    data class TertiaryPick(val hex: String): ThemeTabIntent
    data class TextMainPick(val hex: String): ThemeTabIntent
    data class TextSecondaryPick(val hex: String): ThemeTabIntent
    data class TextDisabledPick(val hex: String): ThemeTabIntent
    data class AccentPick(val hex: String): ThemeTabIntent
    data class OnAccentPick(val hex: String): ThemeTabIntent
    data class DangerPick(val hex: String): ThemeTabIntent
    data class OnDangerPick(val hex: String): ThemeTabIntent
}