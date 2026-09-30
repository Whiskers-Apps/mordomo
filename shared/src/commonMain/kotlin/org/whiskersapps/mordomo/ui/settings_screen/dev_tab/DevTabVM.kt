package org.whiskersapps.mordomo.ui.settings_screen.dev_tab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.whiskersapps.mordomo.core.features.plugins.PluginsRepository
import org.whiskersapps.mordomo.core.features.socket.SocketRepository
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.dev_tab.DevTabState as State

class DevTabVM(
    private val socketRepository: SocketRepository,
    private val pluginsRepository: PluginsRepository
) {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun onIntent(intent: Intent) {
        when (intent) {
            Intent.StartStopClick -> {
                val runningPlugins = state.value.runningPlugins

                if (runningPlugins) {
                    _state.update { it.copy(runningPlugins = false) }

                    CoroutineScope(Dispatchers.IO).launch {
                        socketRepository.killSocket()
                    }

                    return
                }

                _state.update { it.copy(runningPlugins = true) }

                CoroutineScope(Dispatchers.IO).launch {
                    socketRepository.socketJob.start()
                }
            }

            Intent.ReIndexClick -> {
                CoroutineScope(Dispatchers.IO).launch {
                    socketRepository.killSocket()
                    pluginsRepository.index()
                    socketRepository.socketJob.start()
                }
            }
        }
    }
}