package com.world.clock.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
fun getLocalTimeFormatted(timezone: String): String {
    val zone = TimeZone.of(timezone)
    val now = Clock.System.now().toLocalDateTime(zone)

    val hour = if (now.hour % 12 == 0) 12 else now.hour % 12
    val minute = now.minute.toString().padStart(2, '0')
    val second = now.second.toString().padStart(2, '0')
    val amPm = if (now.hour < 12) "AM" else "PM"

    return "$hour:$minute:$second $amPm"
}

fun tickingClock(timezone: String): Flow<String> = flow {
    while (true) {
        emit(getLocalTimeFormatted(timezone))
        delay(1000)
    }
}


