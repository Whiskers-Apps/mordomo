package org.whiskersapps.mordomo.core.features.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val showFooter: Boolean = true,
    /// Map<PluginId, Map<SettingId, Value>>
    val pluginsSettings: Map<String, Map<String, String>> = emptyMap(),
    val searchKeyword: String? = "s",
    val defaultSearchEngine: Int? = 1,
    val searchEngines: List<SearchEngine> = listOf(
        SearchEngine(
            id = 1,
            name = "DuckDuckGo (No AI)",
            query = "https://noai.duckduckgo.com/?q=%s",
            keyword = "!d"
        ),
        SearchEngine(
            id = 2,
            name = "Google",
            query = "https://www.google.com/search?q=%s",
            keyword = "!g"
        ),
        SearchEngine(
            id = 3,
            name = "Ecosia",
            query = "https://www.ecosia.org/search?q=%s",
            keyword = "!e"
        ),
        SearchEngine(
            id = 4,
            name = "Startpage",
            query = "https://www.startpage.com/sp/search?query=%s",
            keyword = "!s"
        )
    ),
)

@Serializable
data class SearchEngine(
    val id: Int,
    val name: String,
    val query: String,
    val keyword: String?,
)
