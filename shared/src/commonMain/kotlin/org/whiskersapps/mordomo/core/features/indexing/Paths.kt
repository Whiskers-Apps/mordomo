package org.whiskersapps.mordomo.core.features.indexing

import java.io.File

fun getApplicationDirs(): List<File> {
    val baseDirs = listOf(
        File("/usr/local/share/applications"),
        File("/usr/share/applications")
    )

    val homeDir = File(System.getProperty("user.home"))
    val homeAppsDir = File(homeDir, ".local/share/applications")

    val xdgDirs = System.getenv("XDG_DATA_DIRS")?.split(":")?.map { File(it, "applications") } ?: emptyList()

    return (baseDirs + homeAppsDir + xdgDirs).distinct().filter { it.exists() && it.isDirectory }
}

fun getApplicationFiles(): List<File> {
    return getApplicationDirs()
        .flatMap { dir ->
            dir.listFiles { file -> file.extension == "desktop" }?.toList() ?: emptyList()
        }
}

fun getCacheDir(): File {
    val homeDir = File(System.getProperty("user.home"))
    val dir = File(homeDir, ".cache/mordomo").apply { mkdirs() }
    return dir
}