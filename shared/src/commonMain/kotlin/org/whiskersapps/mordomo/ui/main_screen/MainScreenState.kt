package org.whiskersapps.mordomo.ui.main_screen

import lib.Entry
import lib.Theme

data class MainScreenState(
    val theme: Theme = Theme(),
    val searchText: String = "",
    val entries: List<Entry> = emptyList(),
    val selectionIndex: Int = 0,
    val focusWindow: Boolean = false,
    val refocusTrigger: Int = 0,
    val context: String = "",
)
