package org.whiskersapps.mordomo.ui.settings_screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import org.whiskersapps.mordomo.ui.settings_screen.SettingsScreenIntent as Intent

class SettingsScreenVM(
    private val windowRepository: WindowRepository
) {
    private val _state = MutableStateFlow(SettingsScreenState())
    val state = _state.asStateFlow()

    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.Back -> {
                windowRepository.goToMain()
            }

            is Intent.TabSelect -> {
                _state.update { it.copy(tab = intent.tab) }
            }
        }
    }
}