package org.whiskersapps.mordomo.ui.settings_screen.plugins_tab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.CheckSetting
import lib.NumberSetting
import lib.SelectSetting
import lib.TextSetting
import org.whiskersapps.mordomo.core.features.plugins.PluginsRepository
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import kotlin.collections.map
import kotlin.collections.mapOf
import org.whiskersapps.mordomo.ui.settings_screen.plugins_tab.PluginsTabState as State

class PluginsTabVM(
    private val pluginsRepository: PluginsRepository,
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        load()

        CoroutineScope(Dispatchers.IO).launch {
            pluginsRepository.manifests.collect { load() }
        }
    }

    fun load() {
        val settings = settingsRepository.settings.value!!.pluginsSettings
        val pluginsValues: MutableMap<String, Map<String, String>> = mutableMapOf()
        val manifests = pluginsRepository.manifests.value

        for ((id) in manifests) {
            pluginsValues[id] = settings[id] ?: emptyMap()
        }

        _state.update {
            State(
                manifests = manifests,
                values = pluginsValues
            )
        }
    }

    fun onIntent(intent: PluginsTabIntent) {
        when (intent) {
            is PluginsTabIntent.SettingChange -> {
                val values = state.value.values.toMutableMap()
                val pluginValues = values[intent.pluginId]!!.toMutableMap()

                pluginValues[intent.settingId] = intent.value

                values[intent.pluginId] = pluginValues

                _state.update { it.copy(values = values) }

                CoroutineScope(Dispatchers.IO).launch {
                    val settings = settingsRepository.settings.value!!
                    val pluginsSettings = settings.pluginsSettings.toMutableMap()
                    val pluginSettings = pluginsSettings[intent.pluginId]!!.toMutableMap()

                    pluginSettings[intent.settingId] = intent.value
                    pluginsSettings[intent.pluginId] = pluginSettings

                    settingsRepository.update(settings.copy(pluginsSettings = pluginsSettings))
                }
            }
        }
    }
}