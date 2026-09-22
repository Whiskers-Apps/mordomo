package org.whiskersapps.mordomo.core.features.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val showFooter: Boolean = true,
    /// Map<PluginId, Map<SettingId, Value>>
    val pluginsSettings: Map<String, Map<String, String>> = emptyMap()
)
