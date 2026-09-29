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

sealed interface GeneralTabIntent: SettingsScreenIntent{}

sealed interface ThemeTabIntent: SettingsScreenIntent{}

sealed interface PluginsTabIntent: SettingsScreenIntent{}

sealed interface AboutTabIntent: SettingsScreenIntent{}

sealed interface DevTabIntent: SettingsScreenIntent{}
