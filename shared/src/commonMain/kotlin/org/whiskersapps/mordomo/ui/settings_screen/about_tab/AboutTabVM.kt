package org.whiskersapps.mordomo.ui.settings_screen.about_tab

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.about_tab.AboutTabState as State

class AboutTabVM {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    init {
        _state.update { State(version = System.getProperty("jpackage.app-version") ?: "x.x.x-dev") }
    }

    fun onIntent(intent: Intent) {
        when(intent) {
            Intent.RepoClick -> {
                ProcessBuilder("xdg-open", "https://github.com/whiskers-apps/mordomo").start()
            }
        }
    }
}