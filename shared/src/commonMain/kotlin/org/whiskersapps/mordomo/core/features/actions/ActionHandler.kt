package org.whiskersapps.mordomo.core.features.actions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.concurrent.thread

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

        fun openUrl(url: String) {
            thread {
                ProcessBuilder("xdg-open", url)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
            }
        }
    }
}