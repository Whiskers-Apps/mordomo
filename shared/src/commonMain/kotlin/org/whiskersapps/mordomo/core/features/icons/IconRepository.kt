package org.whiskersapps.mordomo.core.features.icons

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.whiskersapps.mordomo.core.features.indexing.getCacheDir
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString

val IMAGE_FORMATS = setOf(
    "apng", "png", "avif", "gif", "jpg", "jpeg", "jfif", "pjpeg", "pjp", "svg", "svgz", "webp",
    "bmp", "ico", "cur", "tif", "tiff"
)

class IconRepository {
    private var icons = mutableMapOf<String, String>()
    private var iconPackPath: File? = null
    private var backupDirs: List<File> = emptyList()

    private val _iconsLoaded = Channel<Unit>()
    val iconsLoaded = _iconsLoaded.receiveAsFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            iconPackPath = getIconPackPath(getSystemIconPack())
            backupDirs = getBackupDirs()

            loadFromCache()
            indexIcons()
        }
    }

    private suspend fun loadFromCache() = withContext(Dispatchers.IO) {
        val file = File(getCacheDir(), "icons.json")

        if (!file.exists())
            return@withContext

        try {
            val iconsJson = file.readText()
            icons = Json.decodeFromString(iconsJson)

            _iconsLoaded.send(Unit)
        } catch (e: Exception) {
            println("Failed to read icons cache. $e")
        }
    }

    private fun getSystemIconPack(): String {
        val process = ProcessBuilder(
            "gsettings", "get", "org.gnome.desktop.interface", "icon-theme"
        ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        return output.replace("'", "").replace("\n", "").trim()
    }

    private fun getIconPackPath(name: String): File? {
        for (dir in getIconPacksDirs()) {
            if (!dir.exists()) continue

            dir.walkTopDown()
                .filter { it.isDirectory }
                .forEach { entry ->
                    if (entry.name == name) {
                        return entry
                    }
                }
        }
        return null
    }

    private fun getIconPacksDirs(): List<File> {
        val dirs = mutableListOf<File>()

        val xdgDataDirs = System.getenv("XDG_DATA_DIRS") ?: ""
        for (dir in xdgDataDirs.split(":")) {
            if (dir.isBlank()) continue
            val iconDir = File(dir, "icons")
            addIfExists(dirs, iconDir)
        }

        addIfExists(dirs, File("/usr/share/icons"))

        val home = System.getProperty("user.home")
        addIfExists(dirs, File("$home/.icons"))
        addIfExists(dirs, File("$home/.local/share/icons"))
        addIfExists(dirs, File("$home/.local/share/pixmaps"))

        addIfExists(dirs, File("/var/lib/flatpak/exports/share/icons"))

        return dirs
    }

    private fun addIfExists(dirs: MutableList<File>, dir: File) {
        if (dir.exists() && dir.isDirectory) {
            dirs.add(dir)
        }
    }

    private fun getBackupDirs(): List<File> {
        val home = System.getProperty("user.home")

        val localHicolor = File(home, ".local/share/icons/hicolor")
        val localPixmap = File(home, ".local/share/icons/pixmaps")
        val usrHicolor = File("/usr/share/icons/hicolor")
        val usrPixmap = File("/usr/share/pixmaps")
        val flatpakApps = File("/var/lib/flatpak/app/")
        val homeFlatpakApps = File(home, ".local/share/flatpak")

        val dirs = mutableListOf<File>()

        if (localHicolor.exists()) dirs.add(localHicolor)
        if (localPixmap.exists()) dirs.add(localPixmap)
        if (usrHicolor.exists()) dirs.add(usrHicolor)
        if (usrPixmap.exists()) dirs.add(usrPixmap)
        if (flatpakApps.exists()) dirs.add(flatpakApps)
        if (homeFlatpakApps.exists()) dirs.add(homeFlatpakApps)

        return dirs
    }

    suspend fun indexIcons() = withContext(Dispatchers.IO) {
        val dirs = mutableListOf<File>()

        if (iconPackPath != null)
            dirs.add(iconPackPath!!)

        dirs.addAll(backupDirs)
        dirs.add(File("/usr/share/icons"))
        dirs.add(File(System.getProperty("user.home"), ".local/share/icons"))

        for (dirPath in dirs) {
            if (!dirPath.exists() || !dirPath.isDirectory)
                continue

            dirPath.walkTopDown()
                .onEnter { true }
                .filter { it.isFile }
                .forEach { file ->
                    if (file.extension.lowercase() in IMAGE_FORMATS) {
                        icons.putIfAbsent(file.nameWithoutExtension, file.absolutePath)
                    }
                }
        }

        launch {
            val iconsJson = Json.encodeToString(icons)
            File(getCacheDir(), "icons.json").apply { writeText(iconsJson) }

            _iconsLoaded.send(Unit)
        }
    }

    private fun getTargetPath(path: File): String? {
        if (!Files.isSymbolicLink(path.toPath())) {
            return path.absolutePath
        }

        val link = try {
            Files.readSymbolicLink(path.toPath())
        } catch (_: Exception) {
            return null
        }

        return if (!link.isAbsolute) {
            val parent = path.parentFile ?: return null
            parent.toPath().resolve(link).toRealPath().absolutePathString()
        } else {
            link.toRealPath().absolutePathString()
        }
    }

    fun getIconPath(iconName: String): String? {
        val direct = File(iconName)
        if (direct.exists()) return direct.absolutePath

        val found = icons[iconName] ?: return null

        return getTargetPath(File(found))
    }
}