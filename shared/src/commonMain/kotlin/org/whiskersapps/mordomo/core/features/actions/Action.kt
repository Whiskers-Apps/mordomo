package org.whiskersapps.mordomo.core.features.actions

import kotlinx.serialization.Serializable
import org.whiskersapps.mordomo.core.features.entries.Entry

@Serializable
sealed interface Action

@Serializable
data class OpenApp(
    val text: String,
    val path: String
) : Action

@Serializable
data class OpenUrl(
    val text: String,
    val url: String
) : Action

@Serializable
data class CopyText(
    val text: String,
    val textCopy: String
) : Action

@Serializable
data class CopyImage(
    val text: String,
    val path: String
) : Action

@Serializable
data class ShowEntries(
    val text: String,
    val entries: List<Entry>
) : Action

@Serializable
data class Plugin(
    val text: String,
    val pluginId: String,
    val action: String,
    val customInfo: List<String>
) : Action
