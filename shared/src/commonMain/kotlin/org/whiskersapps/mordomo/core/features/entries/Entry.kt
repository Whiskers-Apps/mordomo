package org.whiskersapps.mordomo.core.features.entries

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import org.whiskersapps.mordomo.core.features.actions.Action

@Serializable
data class Entry(
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val actions: List<Action> = emptyList()
)