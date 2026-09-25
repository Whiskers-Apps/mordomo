package org.whiskersapps.mordomo.core.features.window

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.Form

enum class Route {
    Main, Form, Settings
}

class WindowRepository() {
    private val _showWindow = MutableStateFlow(false)
    val showWindow = _showWindow.asStateFlow()

    private val _route = MutableStateFlow<Route>(Route.Main)
    val route = _route.asStateFlow()

    private val _focusTrigger = Channel<Unit>()
    val focusTrigger = _focusTrigger.receiveAsFlow()

    fun hide() {
        _showWindow.update { false }
    }

    fun show() {
        _showWindow.update { true }
    }

    fun requestFocus() {
        CoroutineScope(Dispatchers.Main).launch {
            _focusTrigger.send(Unit)
        }
    }

    fun goToMain() {
        _route.update { Route.Main }

        CoroutineScope(Dispatchers.Main).launch {
            _focusTrigger.send(Unit)
        }
    }

    fun goToSettings() {
        _route.update { Route.Settings }
    }

    fun goToForm() {
        _route.update { Route.Form }

        CoroutineScope(Dispatchers.Main).launch {
            _focusTrigger.send(Unit)
        }
    }
}