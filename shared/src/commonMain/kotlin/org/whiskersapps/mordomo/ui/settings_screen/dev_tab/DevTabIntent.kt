package org.whiskersapps.mordomo.ui.settings_screen.dev_tab

sealed interface DevTabIntent {
    data object StartStopClick: DevTabIntent
    data object ReIndexClick: DevTabIntent
}