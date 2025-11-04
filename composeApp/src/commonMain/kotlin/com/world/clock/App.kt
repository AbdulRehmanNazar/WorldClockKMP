package com.world.clock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.world.clock.navigation.Screen
import com.world.clock.screens.AllTimeZonesScreen
import com.world.clock.screens.WorldClockScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        val timeZones = listOf("Asia/Karachi", "Europe/London", "America/New_York")
        val favouriteTimeZones = listOf("Asia/Karachi")
        var currentScreen by remember { mutableStateOf<Screen>(Screen.WorldClock(favouriteTimeZones)) }
        when (val screen = currentScreen) {
            is Screen.WorldClock -> {
                WorldClockScreen(timezones = favouriteTimeZones, onAddClick = {
                    currentScreen = Screen.AllTimeZones
                })

            }
            is Screen.AllTimeZones -> {
                AllTimeZonesScreen(timeZones, onBackClick = {
                    currentScreen = Screen.WorldClock(favouriteTimeZones)
                })

            }


        }
    }
}