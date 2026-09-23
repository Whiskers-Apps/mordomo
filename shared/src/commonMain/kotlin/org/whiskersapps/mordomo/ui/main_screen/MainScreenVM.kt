package org.whiskersapps.mordomo.ui.main_screen

import com.whiskersapps.lib.Sniffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.Action
import lib.CopyImage
import lib.CopyText
import lib.Entry
import lib.GetEntries
import lib.KeywordSplit
import lib.OpenApp
import lib.OpenUrl
import lib.Plugin
import lib.RunAction
import lib.ShowEntries
import org.whiskersapps.mordomo.core.features.actions.ActionHandler
import org.whiskersapps.mordomo.core.features.apps.AppsRepository
import org.whiskersapps.mordomo.core.features.plugins.PluginsRepository
import org.whiskersapps.mordomo.core.features.settings.SearchEngine
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import org.whiskersapps.mordomo.core.features.socket.SocketRepository
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState
import org.whiskersapps.mordomo.ui.main_screen.MainScreenIntent as Intent
import org.whiskersapps.mordomo.ui.main_screen.MainScreenState as State

class MainScreenVM(
    val appsRepository: AppsRepository,
    val windowRepository: WindowRepository,
    val socketRepository: SocketRepository,
    val settingsRepository: SettingsRepository,
    val pluginsRepository: PluginsRepository // Nao tirar para que os plugins sejam indexados
) {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val sniffer = Sniffer()

    init {
        _state.update {
            it.copy(
                entries = emptyList()
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
            Intent.SettingsShortcutClick -> onSettingsShortcutClick()
        }
    }

    private fun onSearchTextType(text: String) {
        _state.update { it.copy(searchText = text) }

        scope.launch {

            if (text.isBlank()) {
                _state.update { it.copy(entries = emptyList(), context = "") }
                return@launch
            }

            val split = KeywordSplit(text)
            val settings = settingsRepository.settings.value!!

            if (split.keyword != null) {
                val pluginsKeywords = settingsRepository.pluginsKeywords

                if (pluginsKeywords.containsKey(split.keyword)) {
                    socketRepository.sendToPlugin(pluginsKeywords[split.keyword]!!, GetEntries(split.searchText ?: ""))
                    return@launch
                }

                val searchEngine: SearchEngine? = if( split.keyword == settings.searchKeyword && settings.defaultSearchEngine != null){
                    settings.searchEngines.find { it.id == settings.defaultSearchEngine }
                }else{
                    settings.searchEngines.find { it.keyword == split.keyword }
                }

                if (searchEngine != null) {
                    _state.update {
                        it.copy(
                            context = "Web Search",
                            entries = listOf(
                                Entry(
                                    title = searchEngine.name,
                                    description = "Search for ${split.searchText}",
                                    actions = listOf(
                                        OpenUrl(text = "", url = searchEngine.query.replace("%s", split.searchText ?: ""))
                                    )
                                )
                            )
                        )
                    }

                    return@launch
                }
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

            is Plugin -> {
                scope.launch {
                    socketRepository.sendToPlugin(
                        action.pluginId,
                        message = RunAction(action.action, info = action.customInfo),
                    )

                    windowRepository.hide()
                }
            }

            is ShowEntries -> {
                scope.launch {
                    _state.update { it.copy(entries = action.entries) }
                }
            }
        }

        _state.update { MainScreenState() }
    }

    private fun onSettingsShortcutClick() {
        scope.launch { windowRepository.goToSettings() }
    }
}