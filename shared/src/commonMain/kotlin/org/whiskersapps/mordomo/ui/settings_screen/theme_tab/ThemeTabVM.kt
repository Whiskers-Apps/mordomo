package org.whiskersapps.mordomo.ui.settings_screen.theme_tab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import kotlin.time.Duration.Companion.milliseconds

class ThemeTabVM(
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow(ThemeTabState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        settingsRepository.settings.onEach { settings ->
            _state.update { it.copy(loading = true, theme = settings!!.theme) }
        }.launchIn(scope)
    }


    fun onIntent(intent: ThemeTabIntent) {
        when (intent) {
            is ThemeTabIntent.DarkModeCheck -> {
                _state.update { it.copy(theme = it.theme.copy(dark = intent.dark)) }
                updateTheme()
            }

            is ThemeTabIntent.MainPick -> {
                _state.update { it.copy(theme = it.theme.copy(main = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.SecondaryPick -> {
                _state.update { it.copy(theme = it.theme.copy(secondary = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.TertiaryPick -> {
                _state.update { it.copy(theme = it.theme.copy(tertiary = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.TextMainPick -> {
                _state.update { it.copy(theme = it.theme.copy(textMain = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.TextSecondaryPick -> {
                _state.update { it.copy(theme = it.theme.copy(textSecondary = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.TextDisabledPick -> {
                _state.update { it.copy(theme = it.theme.copy(textDisabled = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.AccentPick -> {
                _state.update { it.copy(theme = it.theme.copy(accent = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.OnAccentPick -> {
                _state.update { it.copy(theme = it.theme.copy(onAccent = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.DangerPick -> {
                _state.update { it.copy(theme = it.theme.copy(danger = intent.hex)) }
                updateTheme()
            }

            is ThemeTabIntent.OnDangerPick -> {
                _state.update { it.copy(theme = it.theme.copy(onDanger = intent.hex)) }
                updateTheme()
            }
        }
    }

    private fun updateTheme() = scope.launch {
        val theme = state.value.theme
        val settings = settingsRepository.settings.value!!.copy(theme = theme)

        settingsRepository.update(settings)
    }
}