package org.whiskersapps.mordomo.ui.settings_screen.plugins_tab

import lib.PluginManifest

data class PluginsTabState(
    val manifests: List<PluginManifest> = emptyList(),
    val values: Map<String, Map<String, String>> = emptyMap()
)
