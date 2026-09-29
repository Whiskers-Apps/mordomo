package org.whiskersapps.mordomo.ui.settings_screen.plugins_tab

sealed interface PluginsTabIntent {
    data class SettingChange(val pluginId: String, val settingId: String, val value: String) : PluginsTabIntent
}