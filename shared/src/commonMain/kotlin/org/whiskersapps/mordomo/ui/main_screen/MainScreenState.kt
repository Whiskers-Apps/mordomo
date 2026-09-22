package org.whiskersapps.mordomo.ui.main_screen

import lib.Entry

data class MainScreenState(
    val searchText: String = "",
    val entries: List<Entry> = emptyList(),
    val selectionIndex: Int = 0,
    val focus: Boolean = false,
    val context: String = "",
)
