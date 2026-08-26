package org.whiskersapps.mordomo.ui.main_screen

sealed interface MainScreenIntent{
    data class SearchTextType(val text: String) : MainScreenIntent
    data object CloseWindow: MainScreenIntent
    data object ArrowUpClick: MainScreenIntent
    data object ArrowDownClick: MainScreenIntent
    data object EnterClick: MainScreenIntent
}