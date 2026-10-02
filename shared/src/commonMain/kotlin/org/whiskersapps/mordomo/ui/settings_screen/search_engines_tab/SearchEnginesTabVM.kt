package org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lib.SearchEngine
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import org.whiskersapps.mordomo.core.utils.addLog
import org.whiskersapps.mordomo.core.utils.getEngineFaviconPath
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.SearchEnginesTabIntent as Intent
import org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab.SearchEnginesTabState as State

class SearchEnginesTabVM(
    private val settingsRepository: SettingsRepository
) {
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var updateKeywordJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        settingsRepository.settings.onEach { settings ->
            _state.update {
                it.copy(
                    loading = false,
                    searchKeyword = settings!!.searchKeyword,
                    defaultEngineId = settings.defaultSearchEngine,
                    searchEngines = settings.searchEngines,
                )
            }
        }.launchIn(scope)
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            is Intent.KeywordType -> {
                _state.update { it.copy(searchKeyword = intent.text) }
                updateKeyword(intent.text)
            }

            Intent.AddClick -> {
                _state.update { it.copy(showAddDialog = true) }
            }

            Intent.AddDialogClose -> {
                _state.update { it.copy(showAddDialog = false) }
            }

            is Intent.SaveNewEngineClick -> {
                scope.launch {
                    _state.update { it.copy(showAddDialog = false) }

                    val newEngines = settingsRepository.settings.value!!.searchEngines.toMutableList()
                    val newId = newEngines.maxOfOrNull { it.id }?.plus(1) ?: 1

                    newEngines.add(
                        SearchEngine(
                            id = newId,
                            name = intent.name,
                            query = intent.query,
                            keyword = intent.keyword,
                        )
                    )

                    val settings = settingsRepository.settings.value!!
                    val defaultEngineId = if (intent.default) newId else settings.defaultSearchEngine

                    val newSettings = settings.copy(defaultSearchEngine = defaultEngineId, searchEngines = newEngines)

                    settingsRepository.update(newSettings)
                }
            }

            is Intent.SaveSearchEngineClick -> {
                scope.launch {
                    val settings = settingsRepository.settings.value!!
                    val searchEngines = settings.searchEngines.toMutableList()
                    val index = searchEngines.indexOfFirst { it.id == intent.searchEngine.id }

                    searchEngines[index] = intent.searchEngine

                    val defaultEngineId = if (intent.default) {
                        intent.searchEngine.id
                    } else if (settings.defaultSearchEngine == intent.searchEngine.id) {
                        null
                    } else {
                        settings.defaultSearchEngine
                    }

                    val newSettings =
                        settings.copy(searchEngines = searchEngines, defaultSearchEngine = defaultEngineId)


                    try {
                        File(getEngineFaviconPath(intent.searchEngine.id)).delete()
                    } catch (e: Exception) {
                        addLog(e)
                    }

                    settingsRepository.update(newSettings)
                }
            }

            is Intent.DeleteEngineClick -> {
                scope.launch {
                    val settings = settingsRepository.settings.value!!
                    val engines = settings.searchEngines.toMutableList().apply {
                        removeIf { it.id == intent.id }
                    }
                    val iconPath = File(getEngineFaviconPath(intent.id))

                    val defaultEngineId = if (settings.defaultSearchEngine == intent.id)
                        null
                    else
                        settings.defaultSearchEngine

                    val newSettings = settings.copy(searchEngines = engines, defaultSearchEngine = defaultEngineId)

                    settingsRepository.update(newSettings)

                    try {
                        iconPath.delete()
                    } catch (e: Exception) {
                        addLog(e)
                    }
                }
            }
        }
    }

    fun updateKeyword(keyword: String) = scope.launch {
        updateKeywordJob?.cancel()

        updateKeywordJob = scope.launch {
            delay(300.milliseconds)

            val settings = settingsRepository.settings.value!!.copy(
                searchKeyword = keyword
            )

            settingsRepository.update(settings)
        }
    }
}