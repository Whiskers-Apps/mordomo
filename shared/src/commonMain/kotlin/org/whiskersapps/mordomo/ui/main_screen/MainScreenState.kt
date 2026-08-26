package org.whiskersapps.mordomo.ui.main_screen

import org.whiskersapps.mordomo.core.features.actions.OpenApp
import org.whiskersapps.mordomo.core.features.apps.App
import org.whiskersapps.mordomo.core.features.entries.Entry

data class MainScreenState(
    val searchText: String = "",
    val entries: List<Entry> = emptyList(),
    val selectionIndex: Int = 0
)
