package org.whiskersapps.mordomo.ui.settings_screen

enum class SettingsTab{
    General,
    Theme,
    Plugins,
    About,
    Dev
}

sealed interface SettingsScreenIntent {
    data object Back : SettingsScreenIntent
    data class TabSelect(val tab: SettingsTab): SettingsScreenIntent
}