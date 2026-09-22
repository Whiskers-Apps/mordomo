package org.whiskersapps.mordomo.core.features.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lib.CheckSetting
import lib.NumberSetting
import lib.PluginManifest
import lib.SelectSetting
import lib.TextSetting
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import java.io.File
import java.util.Collections.emptyMap

class PluginsRepository(
    private val settingsRepository: SettingsRepository
) {
    companion object {
        val PLUGINS_DIR = File(System.getProperty("user.home"), ".local/share/mordomo/plugins")
    }

    val manifests = ArrayList<PluginManifest>()

    init {
        CoroutineScope(IO).launch {
            PLUGINS_DIR.walkTopDown()
                .filter { it.isFile && it.name == "manifest.json" }
                .forEach { file ->
                    try {
                        val jsonContent = file.readText()
                        val manifest: PluginManifest = Json.decodeFromString(jsonContent)

                        var pluginSettings: MutableMap<String, String> =
                            settingsRepository.settings.value!!.pluginsSettings[manifest.id]?.toMutableMap()
                                ?: emptyMap<String, String>().toMutableMap()

                        for (setting in manifest.settings) {
                            val id = when (setting) {
                                is CheckSetting -> setting.id
                                is NumberSetting -> setting.id
                                is SelectSetting -> setting.id
                                is TextSetting -> setting.id
                            }

                            if (!pluginSettings.containsKey(id)) {
                                val value = when (setting) {
                                    is CheckSetting -> setting.value.toString()
                                    is NumberSetting -> setting.value.toString()
                                    is SelectSetting -> setting.defaultOptionId
                                    is TextSetting -> setting.value
                                }

                                pluginSettings[id] = value
                            }
                        }

                        if (!pluginSettings.containsKey("[keyword]")) {
                            pluginSettings["[keyword]"] = ""
                        }

                        val currentSettings = settingsRepository.settings.value!!
                        val updatedPluginsSettings = currentSettings.pluginsSettings + (manifest.id to pluginSettings)
                        val newSettings = currentSettings.copy(pluginsSettings = updatedPluginsSettings)

                        settingsRepository.update(newSettings)

                        manifests.add(manifest)

                        val pluginFile = File(file.parent, "plugin.jar")

                        if (!pluginFile.exists()) {
                            println("Binary for [${manifest.id}] not found")
                            return@forEach
                        }

                        pluginFile.setExecutable(true)

                        launch(IO) {
                            println("Executing [${manifest.id}]")

                            ProcessBuilder(getJavaBin(), "-jar", pluginFile.path)
                                .start()
                        }
                    } catch (_: Exception) {
                        println("Failed to decode manifest. [${file.path}]")
                    }
                }
        }
    }

    // This function is very vibe coded xD
    private fun getJavaBin(): String {
        val javaHome = System.getProperty("java.home")
        val userHome = System.getProperty("user.home")

        if (!javaHome.isNullOrBlank()) {
            val directJava = File(javaHome, "bin/java")
            if (directJava.canExecute()) return directJava.absolutePath
        }

        val envJavaHome = System.getenv("JAVA_HOME")
        if (!envJavaHome.isNullOrBlank()) {
            val envJava = File(envJavaHome, "bin/java")
            if (envJava.canExecute()) return envJava.absolutePath
        }

        if (!userHome.isNullOrBlank()) {
            val userPaths = listOf(
                "$userHome/.sdkman/candidates/java/current/bin/java",
                "$userHome/.local/bin/java",
                "$userHome/bin/java",
                "$userHome/.asdf/shims/java",
                "$userHome/.local/share/mise/shims/java"
            )
            for (path in userPaths) {
                val file = File(path)
                if (file.canExecute()) return file.absolutePath
            }

            val jdksDir = File(userHome, ".jdks")
            if (jdksDir.isDirectory) {
                val ideaJava = jdksDir.listFiles()
                    ?.map { File(it, "bin/java") }
                    ?.firstOrNull { it.canExecute() }
                if (ideaJava != null) return ideaJava.absolutePath
            }
        }

        val systemPaths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (dir in systemPaths) {
            val pathJava = File(dir, "java")
            if (pathJava.canExecute()) return pathJava.absolutePath
        }

        val systemFallbacks = listOf(
            "/usr/bin/java",
            "/usr/lib/jvm/default/bin/java",
            "/usr/lib/jvm/default-runtime/bin/java"
        )
        for (fallback in systemFallbacks) {
            val f = File(fallback)
            if (f.canExecute()) return f.absolutePath
        }

        return "java"
    }
}