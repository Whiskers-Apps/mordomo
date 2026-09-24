package org.whiskersapps.mordomo

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.whiskersapps.mordomo.ui.main_screen.MainScreen
import org.whiskersapps.mordomo.ui.main_screen.MainScreenRoot
import org.whiskersapps.mordomo.ui.main_screen.MainScreenVM


import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin

import org.whiskersapps.mordomo.core.features.apps.AppsRepository
import org.whiskersapps.mordomo.core.features.icons.IconRepository
import org.whiskersapps.mordomo.core.features.plugins.PluginsRepository
import org.whiskersapps.mordomo.core.features.settings.SettingsRepository
import org.whiskersapps.mordomo.core.features.socket.SocketRepository
import org.whiskersapps.mordomo.core.features.window.Route
import org.whiskersapps.mordomo.core.features.window.WindowRepository

val appModule = module {
    single { SocketRepository(get()) }
    single { WindowRepository() }
    single { IconRepository() }
    single { AppsRepository(get()) }
    single { SettingsRepository() }
    single { PluginsRepository(get()) }

    single { MainScreenVM(get(), get(), get(), get(), get()) }
}

fun main() {
    startKoin { modules(appModule) }

    val windowRepository = getKoin().get<WindowRepository>()
    val socketRepository = getKoin().get<SocketRepository>()
    val settingsRepository = getKoin().get<SettingsRepository>()

    Runtime.getRuntime().addShutdownHook(Thread {
        CoroutineScope(Dispatchers.Default).launch {
            socketRepository.killSocket()
        }
    })

    application {
        val showWindow = windowRepository.showWindow.collectAsState().value
        val settings = settingsRepository.settings.collectAsState().value
        val route = windowRepository.route.collectAsState().value

        if (settings != null) {
            Window(
                state = rememberWindowState(
                    position = WindowPosition(Alignment.Center),
                    size = DpSize(800.dp, 500.dp)
                ),
                onCloseRequest = { windowRepository.hide() },
                title = "Mordomo",
                undecorated = true,
                alwaysOnTop = true,
                resizable = false,
                visible = showWindow
            ) {
                LaunchedEffect(showWindow) {
                    if (showWindow) {
                        window.toFront()
                        window.requestFocus()
                    }
                }

                when (route) {
                    Route.Main -> {
                        MainScreenRoot()
                    }

                    Route.Settings -> {

                    }
                }
            }
        }
    }
}