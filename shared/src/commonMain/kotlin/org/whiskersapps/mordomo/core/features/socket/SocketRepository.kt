package org.whiskersapps.mordomo.core.features.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.whiskersapps.mordomo.core.features.window.WindowRepository
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import kotlin.system.exitProcess

class SocketRepository(val windowRepository: WindowRepository) {
    companion object {
        val SOCKET_FILE = File("/tmp/mordomo.port")
    }

    var socket: ServerSocket? = null
    var socketPort: Int = 0

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val loaded = loadFromFile()

            if (!loaded)
                initSocket()

            listen()
        }
    }

    private suspend fun loadFromFile(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!SOCKET_FILE.exists())
                return@withContext false

            socketPort = SOCKET_FILE.readText().toIntOrNull() ?: return@withContext false

            Socket("localhost", socketPort).use {
                it.getOutputStream().write("show".toByteArray())
                exitProcess(0)
            }
        } catch (e: Exception) {
            println("Failed to load from file. $e")
            return@withContext false
        }
    }

    private suspend fun initSocket() = withContext(Dispatchers.IO) {
        try {
            socket = ServerSocket(0)
            socketPort = socket!!.localPort

            SOCKET_FILE.writeText(socketPort.toString())

            windowRepository.show()
        } catch (e: Exception) {
            println("Failed to create socket. $e")
        }
    }

    suspend fun killSocket() = withContext(Dispatchers.IO) {
        socket?.close()
        SOCKET_FILE.delete()
    }

    private suspend fun listen() = withContext(Dispatchers.IO) {
        socket?.let { socket ->
            while (!socket.isClosed) {
                try {
                    socket.accept().use { client ->
                        val message = client.getInputStream().bufferedReader().readLine() ?: continue

                        when(message){
                            "show" -> windowRepository.show()
                        }
                    }
                } catch (_: Exception) {
                    break
                }
            }
        }
    }
}