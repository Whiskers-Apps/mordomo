package org.whiskersapps.mordomo.ui.settings_screen.about_tab

sealed interface AboutTabIntent {
    data object RepoClick: AboutTabIntent
}