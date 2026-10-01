package org.whiskersapps.mordomo.ui.settings_screen.theme_tab

import lib.Theme

data class ThemeTabState(
    val loading: Boolean = true,
    val theme: Theme = Theme(),
)
