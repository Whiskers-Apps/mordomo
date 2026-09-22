package org.whiskersapps.mordomo.core.features.window

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whiskersapps.mordomo.core.features.socket.SocketRepository

enum class Route{
    Main,
    Settings
}

class WindowRepository() {
    private val _showWindow = MutableStateFlow(false)
    val showWindow = _showWindow.asStateFlow()

    private val _route = MutableStateFlow(Route.Main)
    val route = _route.asStateFlow()

    fun hide() {
        _showWindow.update { false }
    }

    fun show() {
        _showWindow.update { true }
    }

    fun goToMain(){
        _route.update { Route.Main }
    }

    fun goToSettings(){
        _route.update { Route.Settings }
    }
}