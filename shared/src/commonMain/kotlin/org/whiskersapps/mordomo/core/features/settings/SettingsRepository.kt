package org.whiskersapps.mordomo.core.features.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class SettingsRepository {
    private val _settings = MutableStateFlow<Settings?>(null)
    val settings: StateFlow<Settings?> = _settings

    val jsonConf = Json { prettyPrint = true; encodeDefaults = true }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val file = getConfigFile()

            if (!file.exists()) {
                file.createNewFile()

                val defaultSettings = Settings()

                val settingsJson = jsonConf.encodeToString(defaultSettings)
                file.writeText(settingsJson)

                _settings.update { defaultSettings }

                return@launch
            }

            try {
                val fileSettings: Settings = jsonConf.decodeFromString(file.readText())
                _settings.update { fileSettings }
            } catch (e: Exception) {
                println("Failed to parse settings from file (Using default as a backup). $e")
                _settings.update { Settings() }
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
    }
}