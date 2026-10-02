package org.whiskersapps.mordomo.core.utils

import java.io.File

val LOG_FILE = File(System.getProperty("user.home"), ".cache/mordomo/mordomo.log")

fun addLog(e: Exception) {
    if (!LOG_FILE.exists())
        LOG_FILE.createNewFile()

    LOG_FILE.appendText("---------------------------------------------------------------------------------------------\n$e\n")
    println(e.toString())
}