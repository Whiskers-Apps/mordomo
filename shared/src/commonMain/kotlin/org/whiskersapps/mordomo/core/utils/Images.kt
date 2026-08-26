package org.whiskersapps.mordomo.core.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.io.File
import java.io.InputStream

fun getImageFromPath(path: String, density: Density): Painter? {
    val file = File(path)
    if (!file.exists()) return null

    return try {
        file.inputStream().buffered().use { stream ->
            if (file.extension.lowercase() == "svg") {
                stream.readAllBytes().decodeToSvgPainter(density)
            } else {
                BitmapPainter(stream.readAllBytes().decodeToImageBitmap())
            }
        }
    } catch (e: Exception) {
        println("Failed to get image from $path. $e")
        null
    }
}