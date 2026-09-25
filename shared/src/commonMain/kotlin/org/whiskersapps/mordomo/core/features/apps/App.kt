package org.whiskersapps.mordomo.core.features.apps

import kotlinx.serialization.Serializable
import lib.Entry
import lib.OpenApp

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
            action = OpenApp(this.path)
        )
    }
}
