package org.whiskersapps.mordomo.ui.settings_screen.general_tab

sealed interface GeneralTabIntent {
    data class HideFooterCheck(val hidden: Boolean) : GeneralTabIntent
    data class DrawBorderCheck(val draw: Boolean) : GeneralTabIntent
}