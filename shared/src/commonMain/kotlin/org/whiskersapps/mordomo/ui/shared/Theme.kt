package org.whiskersapps.mordomo.ui.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MordomoTheme(
    content: @Composable () -> Unit
){
    MaterialTheme(
        colorScheme = getMaterialMonoCode(ThemeId.LynxYellow),
        content = content
    )
}