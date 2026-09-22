package org.whiskersapps.mordomo.ui.main_screen

import com.whiskersapps.lib.Sniffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.Action
import lib.CopyImage
import lib.CopyText
import lib.Entry
import lib.GetEntries
import lib.OpenApp
import lib.OpenUrl
import lib.Plugin
import lib.PluginMessage
import lib.ShowEntries
import org.whiskersapps.mordomo.core.features.actions.ActionHandler
import org.whiskersapps.mordomo.core.features.apps.AppsRepository
import org.whiskersapps.mordomo.core.features.plugins.PluginsRepository
import org.whiskersapps.mordomo.core.features.socket.SocketRepository
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState
import org.whiskersapps.mordomo.ui.main_screen.MainScreenIntent as Intent
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState as State

class MainScreenVM(
    val appsRepository: AppsRepository,
    val windowRepository: WindowRepository,
    val socketRepository: SocketRepository,
    val pluginsRepository: PluginsRepository
) {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val sniffer = Sniffer()

    init {
        _state.update {
            it.copy(
                entries = listOf(
                    Entry(title = "Test Open App", actions = listOf(OpenApp("", "firefox.desktop"))),
                    Entry(title = "Test Copy Text", actions = listOf(CopyText("", "lorem ipsum"))),
                    Entry(
                        title = "Test Copy Image", actions = listOf(
                            CopyImage(
                                "",
                                "/home/lighttigerxiv/Pictures/profile/tiger.jpg"
                            )
                        )
                    ),
                    Entry(
                        title = "Test Open Url", actions = listOf(
                            OpenUrl(
                                "",
                                "https://noai.duckduckgo.com/&q=lorem ipsum"
                            )
                        )
                    ),
                    Entry(
                        title = "Test Show Entries", actions = listOf(
                            ShowEntries(
                                "", listOf(
                                    Entry(title = "1", actions = listOf(CopyText("", "1"))),
                                    Entry(title = "2", actions = listOf(CopyText("", "2"))),
                                    Entry(title = "3", actions = listOf(CopyText("", "3"))),
                                )
                            )
                        )
                    ),
                )
            )
        }

        socketRepository.pluginResponse.onEach { entries ->
            _state.update { it.copy(entries = entries, context = "Plugin") }
        }.launchIn(CoroutineScope(IO))

        windowRepository.showWindow.onEach { showWindow ->
            _state.update { it.copy(focus = showWindow) }

        }.launchIn(CoroutineScope(IO))
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.SearchTextType -> onSearchTextType(intent.text)
            Intent.CloseWindow -> onEscapeClick()
            Intent.ArrowDownClick -> onArrowDownClick()
            Intent.ArrowUpClick -> onArrowUpClick()
            Intent.EnterClick -> onEnterClick()
        }
    }

    private fun onSearchTextType(text: String) {
        _state.update { it.copy(searchText = text) }

        scope.launch {

            if (text.isBlank()) {
                _state.update { it.copy(entries = emptyList()) }
                return@launch
            }

            if (text.startsWith("t ")) {
                socketRepository.sendToPlugin("core-testing", GetEntries(text))
                return@launch
            }

            val apps = appsRepository.apps
                .filter { sniffer.matches(it.name, text) }
                .map { it.asEntry() }

            _state.update { it.copy(entries = apps, selectionIndex = 0, context = "Apps") }
        }
    }

    private fun onEscapeClick() {
        scope.launch {
            _state.update { MainScreenState() }

            windowRepository.hide()
        }
    }

    private fun onArrowDownClick() {
        scope.launch {
            val newIndex = state.value.selectionIndex + 1

            if (newIndex >= state.value.entries.size)
                return@launch

            _state.update { it.copy(selectionIndex = newIndex) }
        }
    }

    private fun onArrowUpClick() {
        scope.launch {
            val newIndex = state.value.selectionIndex - 1

            if (newIndex < 0)
                return@launch

            _state.update { it.copy(selectionIndex = newIndex) }
        }
    }

    private fun onEnterClick() {
        scope.launch {
            val actions = state.value.entries[state.value.selectionIndex].actions

            if (actions.isEmpty())
                return@launch

            if (actions.size == 1) {
                handleAction(actions[0])
            }
            // Mostrar menu com mais opcoes
        }
    }

    private fun handleAction(action: Action) {
        when (action) {
            is CopyImage -> {
                scope.launch {
                    ActionHandler.copyImage(action.path)
                    windowRepository.hide()
                }
            }

            is CopyText -> {
                scope.launch {
                    ActionHandler.copyText(action.textCopy)
                    windowRepository.hide()
                }
            }

            is OpenApp -> {
                appsRepository.openApp(action.path)
                windowRepository.hide()
            }

            is OpenUrl -> {
                scope.launch {
                    ActionHandler.openUrl(action.url)
                    windowRepository.hide()
                }
            }

            is Plugin -> TODO()
            is ShowEntries -> {
                scope.launch {
                    _state.update { it.copy(entries = action.entries) }
                }
            }
        }

        _state.update { MainScreenState() }
    }
}