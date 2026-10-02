package org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab

import lib.SearchEngine

data class SearchEnginesTabState(
    val loading: Boolean = true,
    val searchKeyword: String? = null,
    val defaultEngineId: Int? = 0,
    val searchEngines: List<SearchEngine> = emptyList(),
    val showAddDialog: Boolean = false,
)
