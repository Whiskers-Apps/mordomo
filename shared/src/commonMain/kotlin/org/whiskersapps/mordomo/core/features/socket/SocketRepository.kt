package org.whiskersapps.mordomo.core.features.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import lib.Action
import lib.Entry
import lib.PluginMessage
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

class SocketRepository(val windowRepository: WindowRepository) {
    companion object {
        val SOCKET_FILE = File("/tmp/mordomo.port")
    }

    val clients = ConcurrentHashMap<String, PrintWriter>()

    private val _pluginResponse = Channel<List<Entry>>()
    val pluginResponse = _pluginResponse.consumeAsFlow()


    init {
        CoroutineScope(Dispatchers.IO).launch {
            val loaded = loadFromFile()

            if (!loaded)
                socketJob.start()
        }
    }

    private suspend fun loadFromFile(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!SOCKET_FILE.exists())
                return@withContext false

            val port = SOCKET_FILE.readText().toIntOrNull() ?: return@withContext false

            val socket = Socket("localhost", port)
            val transmitter = PrintWriter(socket.getOutputStream(), true)

            transmitter.println("show")

            exitProcess(0)
        } catch (e: Exception) {
            println("Failed to load from file. $e")
            return@withContext false
        }
    }


    private val socketJob = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
        val server = ServerSocket(0)
        val port = server.localPort

        SOCKET_FILE.writeText(port.toString())

        windowRepository.show()

        try {
            while (true) {
                val client = server.accept()

                launch(Dispatchers.IO) {
                    val receiver = BufferedReader(InputStreamReader(client.getInputStream()))
                    var pluginId: String? = null

                    try {
                        while (true) {
                            val message = receiver.readLine() ?: break

                            if (message.startsWith("ack ")) {
                                val messageParts = message.split(" ")
                                pluginId = messageParts[1]

                                clients[pluginId] = PrintWriter(client.getOutputStream(), true)
                                continue
                            }

                            if (message == "show") {
                                windowRepository.show()
                                continue
                            }

                            val entries = Json.decodeFromString<List<Entry>>(message)
                            _pluginResponse.send(entries)
                        }
                    } finally {
                        try {
                            pluginId?.let { clients.remove(it) }
                            client.close()
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        } catch (_: Exception) {
            println()
        }
    }

    suspend fun sendToPlugin(pluginId: String, message: PluginMessage) = withContext(Dispatchers.IO) {
        clients[pluginId]?.println(Json.encodeToString(message))
    }

    suspend fun killSocket() = withContext(Dispatchers.IO) {
        socketJob.cancel()
        SOCKET_FILE.delete()
    }
}