package org.whiskersapps.mordomo.ui.main_screen

import lib.Entry

data class MainScreenState(
    val searchText: String = "",
    val entries: List<Entry> = emptyList(),
    val selectionIndex: Int = 0,
    val focusWindow: Boolean = false,
    val refocusTrigger: Int = 0,
    val context: String = "",
)
