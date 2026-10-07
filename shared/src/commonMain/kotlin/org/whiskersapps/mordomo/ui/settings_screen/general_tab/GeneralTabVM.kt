package org.whiskersapps.mordomo.ui.settings_screen.general_tab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository

class GeneralTabVM(
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow(GeneralTabState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        settingsRepository.settings.onEach { settings ->
            if (settings == null) return@onEach

            _state.update {
                it.copy(
                    loading = false,
                    hideFooter = settings.hideFooter,
                    drawBorder = settings.drawBorder
                )
            }
        }.launchIn(scope)
    }

    fun onIntent(intent: GeneralTabIntent) {
        when (intent) {
            is GeneralTabIntent.HideFooterCheck -> {
                _state.update { it.copy(hideFooter = intent.hidden) }

                scope.launch {
                    val settings = settingsRepository.settings.value!!.copy(
                        hideFooter = intent.hidden
                    )

                    settingsRepository.update(settings)
                }
            }

            is GeneralTabIntent.DrawBorderCheck -> {
                _state.update { it.copy(drawBorder = intent.draw) }

                scope.launch {
                    val settings = settingsRepository.settings.value!!.copy(
                        drawBorder = intent.draw
                    )

                    settingsRepository.update(settings)
                }
            }
        }
    }
}