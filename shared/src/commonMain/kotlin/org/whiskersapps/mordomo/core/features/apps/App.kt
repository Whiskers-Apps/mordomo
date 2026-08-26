package org.whiskersapps.mordomo.core.features.apps

import kotlinx.serialization.Serializable
import org.whiskersapps.mordomo.core.features.actions.OpenApp
import org.whiskersapps.mordomo.core.features.actions.OpenUrl
import org.whiskersapps.mordomo.core.features.entries.Entry

@Serializable
data class App(
    val name: String,
    val description: String?,
    val keywords: List<String>,
    val path: String,
    val iconPath: String?
) {
    fun asEntry(): Entry {
        return Entry(
            image = this.iconPath,
            title = this.name,
            description = this.description ?: "Application",
            actions = listOf(
                OpenApp("", this.path),
            )
        )
    }
}
