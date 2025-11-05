package com.world.clock

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.world.clock.screens.worldclock.FavouriteTimeZonesScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {



        Navigator(
            screen = FavouriteTimeZonesScreen(),
        ){ navigator ->
            SlideTransition(navigator){screen->
                screen.Content()
            }
        }

    }
}