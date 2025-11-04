package com.world.clock.navigation

sealed class Screen {
    data class WorldClock(val timeZone: List<String>) : Screen()
    object AllTimeZones : Screen()
}