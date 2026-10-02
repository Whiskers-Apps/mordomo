package org.whiskersapps.mordomo.ui.settings_screen.search_engines_tab

import lib.SearchEngine

sealed interface SearchEnginesTabIntent {
    data class KeywordType(val text: String) : SearchEnginesTabIntent
    data object AddClick: SearchEnginesTabIntent
    data object AddDialogClose: SearchEnginesTabIntent
    data class SaveNewEngineClick(val name: String, val keyword: String, val query: String, val default: Boolean) : SearchEnginesTabIntent
    data class SaveSearchEngineClick(val searchEngine: SearchEngine, val default: Boolean) : SearchEnginesTabIntent
    data class DeleteEngineClick(val id: Int) : SearchEnginesTabIntent
}