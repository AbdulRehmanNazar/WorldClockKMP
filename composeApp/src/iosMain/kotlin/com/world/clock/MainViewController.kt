package com.world.clock

import androidx.compose.ui.window.ComposeUIViewController
import com.world.clock.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) { App() }