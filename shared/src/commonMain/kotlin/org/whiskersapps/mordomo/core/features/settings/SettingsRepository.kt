package org.whiskersapps.mordomo.core.features.settings

import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.whiskersapps.mordomo.core.utils.getFaviconURL
import org.whiskersapps.mordomo.core.utils.getImageFromPath
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SettingsRepository {
    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings

    val jsonConf = Json { prettyPrint = true; encodeDefaults = true }

    /// Map<Keyword, PluginId>
    val pluginsKeywords: MutableMap<String, String> = mutableMapOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val file = getConfigFile()

            if (!file.exists()) {
                file.createNewFile()

                val defaultSettings = Settings()

                val settingsJson = jsonConf.encodeToString(defaultSettings)
                file.writeText(settingsJson)

                _settings.update { defaultSettings }
                downloadFavicons()

                return@launch
            }

            try {
                val fileSettings: Settings = jsonConf.decodeFromString(file.readText())
                _settings.update { fileSettings }

                assignKeywords()
                downloadFavicons()
            } catch (e: Exception) {
                println("Failed to parse settings from file (Using default as a backup). $e")
                _settings.update { Settings() }
                downloadFavicons()
            }
        }
    }

    private fun getConfigFile(): File {
        val dir = File(System.getProperty("user.home"), ".config/mordomo").apply { mkdirs() }
        return File(dir, "settings.json")
    }

    suspend fun update(settings: Settings) = withContext(Dispatchers.IO) {
        _settings.update { settings }
        getConfigFile().writeText(jsonConf.encodeToString(settings))

        assignKeywords()
    }

    private fun assignKeywords() {
        pluginsKeywords.clear()

        val pluginsSettings = settings.value!!.pluginsSettings

        for (pluginId in pluginsSettings.keys) {
            val keyword = pluginsSettings[pluginId]?.get("[keyword]") ?: continue

            pluginsKeywords[keyword] = pluginId
        }
    }

    private suspend fun downloadFavicons() = withContext(Dispatchers.IO) {
        try {
            val iconsCacheDir = File(System.getProperty("user.home"), ".cache/mordomo/favicons").apply { mkdirs() }
            val searchEngines = settings.value!!.searchEngines
            val client = HttpClient.newHttpClient()

            for ((id, _, query) in searchEngines) {
                val iconPath = File(iconsCacheDir, "${id}.png")

                if (iconPath.exists()) continue

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(getFaviconURL(query)))
                    .GET()
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
                val imageBytes = response.body()

                iconPath.writeBytes(imageBytes)

                delay(1000.milliseconds)
            }
        } catch (e: Exception) {
            println("Failed to download favicons. $e")
        }
    }
}