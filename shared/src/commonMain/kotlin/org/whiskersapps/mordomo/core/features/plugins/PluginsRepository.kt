package org.whiskersapps.mordomo.core.features.plugins

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import lib.PluginManifest
import java.io.File

class PluginsRepository {
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

    private fun getJavaBin(): String {
        val javaHome = System.getProperty("java.home")
        val bundledJava = File(javaHome, "bin/java")
        if (bundledJava.exists() && bundledJava.canExecute()) {
            return bundledJava.absolutePath
        }

        val envJavaHome = System.getenv("JAVA_HOME")
        if (!envJavaHome.isNullOrBlank()) {
            val envJava = File(envJavaHome, "bin/java")
            if (envJava.exists() && envJava.canExecute()) {
                return envJava.absolutePath
            }
        }

        val systemPaths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
        for (path in systemPaths) {
            val pathJava = File(path, "java")
            if (pathJava.exists() && pathJava.canExecute()) {
                return pathJava.absolutePath
            }
        }

        return "java"
    }
}