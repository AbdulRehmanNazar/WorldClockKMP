package com.world.clock

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.world.clock.di.initKoinDesktop

fun main() = application {
    initKoinDesktop()
    Window(
        onCloseRequest = ::exitApplication,
        title = "WorldClockKMP",
    ) {
        App()
    }
}