package org.whiskersapps.mordomo.core.features.apps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whiskersapps.mordomo.core.features.icons.IconRepository
import org.whiskersapps.mordomo.core.features.indexing.getApplicationFiles
import org.whiskersapps.mordomo.core.features.indexing.getCacheDir
import java.io.File
import kotlinx.serialization.json.Json
import org.whiskersapps.mordomo.core.features.indexing.getApplicationDirs
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey
import kotlin.concurrent.thread


class AppsRepository(
    val iconRepository: IconRepository
) {
    var apps = emptyList<App>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadAppsFromCache()
            index()

            iconRepository.iconsLoaded.collect { index() }
        }

        CoroutineScope(Dispatchers.IO).launch {
            watchChanges()
        }
    }

    fun loadAppsFromCache() {
        val file = File(getCacheDir(), "apps.json")

        if (!file.exists())
            return

        try {
            val json = file.readText()
            apps = Json.decodeFromString(json)
        } catch (e: Exception) {
            println("Failed to read apps cache. $e")
        }
    }

    private suspend fun watchChanges() = withContext(Dispatchers.IO) {
        val watchService = FileSystems.getDefault().newWatchService()
        val watchedDirs = mutableMapOf<WatchKey, Path>()

        for (dir in getApplicationDirs()) {
            if (!dir.exists()) continue

            val key = dir.toPath().register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )

            watchedDirs[key] = dir.toPath()
        }

        while (isActive) {
            val key = try {
                watchService.take()
            } catch (e: Exception) {
                println("Failed to watch changes. $e")
                break
            }

            key.pollEvents()
            key.reset()

            index()
        }
    }

    private suspend fun index() = withContext(Dispatchers.IO) {
        val files = getApplicationFiles()
        val newApps = ArrayList<App>()

        for (file in files) {
            val content = file.readText()

            val lines = content.lines()
            val fields = mutableMapOf<String, String>()

            var readingDesktopSection = false

            for (line in lines) {
                if (line.isBlank() || line.startsWith("#"))
                    continue

                if (readingDesktopSection && line.startsWith("["))
                    break

                if (line.startsWith("[Desktop Entry]"))
                    readingDesktopSection = true

                if (!readingDesktopSection)
                    continue

                val parts = line.split("=", limit = 2)

                if (parts.size != 2)
                    continue

                fields[parts[0]] = parts[1]
            }

            val isApp = fields["Type"] == "Application"

            if (!isApp)
                continue

            val noDisplay = fields["NoDisplay"] == "true"

            if (noDisplay)
                continue

            newApps.add(
                App(
                    name = fields["Name"] ?: "",
                    description = fields["Comment"],
                    keywords = fields["Keywords"]?.split(";") ?: emptyList(),
                    path = file.path,
                    iconPath = if (fields["Icon"] != null) iconRepository.getIconPath(fields["Icon"]!!)?.path else null
                )
            )
        }

        newApps.sortBy { it.name.lowercase() }

        val appsJson = Json.encodeToString(newApps)

        File(getCacheDir(), "apps.json").apply { writeText(appsJson) }

        apps = newApps
    }

    fun openApp(path: String) {
        val name = File(path).name

        thread {
            try {
                ProcessBuilder("gtk-launch", name)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()

            } catch (e: Exception) {
                println("Failed to open app. $e")
            }
        }
    }
}