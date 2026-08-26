package org.whiskersapps.mordomo.core.features.actions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ActionHandler {
    companion object {
        suspend fun copyText(text: String) {
            withContext(Dispatchers.IO) {
                val textParts = text.split(" ")
                val command = listOf("wl-copy") + textParts

                ProcessBuilder(command).start()
            }
        }

        suspend fun copyImage(path: String) {
            withContext(Dispatchers.IO) {
                ProcessBuilder("wl-copy", "-t", "image/png")
                    .redirectInput(File(path))
                    .start()
            }
        }

        suspend fun openUrl(url: String) {
            withContext(Dispatchers.IO) {
                ProcessBuilder("xdg-open", url).start()
            }
        }
    }
}