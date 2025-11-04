package com.world.clock.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.world.clock.utils.tickingClock


@Composable
fun WorldClockScreen(timezones: List<String>, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "\u2795",
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier.clickable {
                    onAddClick()
                })
        }

        timezones.forEach { tz ->
            TickingClockView(timezone = tz)
        }
    }
}


@Composable
fun TickingClockView(timezone: String) {
    var currentTime by remember { mutableStateOf("") }


    LaunchedEffect(timezone) {
        tickingClock(timezone).collect { time ->
            currentTime = time
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = timezone,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(text = currentTime, style = MaterialTheme.typography.bodyLarge)
    }
}