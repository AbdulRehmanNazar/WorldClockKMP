package com.world.clock.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.data.getDatabaseBuilder
import com.world.clock.screens.worldclock.TickingClockView
import com.world.clock.utils.getGmtOffsetString
import com.world.clock.utils.getLocalTimeFormatted
import com.world.clock.utils.tickingClock
import com.world.clock.utils.timeZones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.ffavourite
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class AllTimeZonesScreen() : Screen {


    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db = remember { getDatabaseBuilder() }
        val dao = remember { db.favouriteTimeZoneDao() }

        BackHandler(enabled = true) {
            navigator.pop()
        }


        AllTimeZonesScreenContent(
            timeZones,
            onTimeZoneClick = { timeZone ->
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insert(FavouriteTimeZone(timeZone))
                }
                navigator.pop()
            },
        )
    }


}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllTimeZonesScreenContent(
    timezones: List<String>,
    onTimeZoneClick: (String) -> Unit
) {


    var searchQuery by remember { mutableStateOf("") }

    val filteredTimeZones = remember(searchQuery, timezones) {
        if (searchQuery.isBlank()) timezones
        else timezones.filter { it.contains(searchQuery, ignoreCase = true) }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.sdp),
        verticalArrangement = Arrangement.spacedBy(16.sdp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 30.sdp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "World Clock",
                color = Color.Black,
                fontSize = 20.ssp
            )
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search time zones...") },
            singleLine = true
        )
        LazyColumn {
            items(filteredTimeZones) { tz ->
                AllTimeZoneItem(timezone = tz, onTimeZoneClick = { timeZone ->
                    onTimeZoneClick(timeZone)

                })
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AllTimeZoneItem(
    timezone: String,
    isFavourite: Boolean = false,
    onTimeZoneClick: (String) -> Unit
) {
    var currentTime by remember { mutableStateOf(getLocalTimeFormatted(timezone)) }

    val zone = remember(timezone) { TimeZone.of(timezone) }

    val offsetString = remember(zone) { getGmtOffsetString(zone) }

    val displayName = remember(timezone) {
        timezone.substringAfterLast('/').replace('_', ' ')
    }

    // Update ticking time every second
    LaunchedEffect(timezone) {
        tickingClock(timezone).collect { time ->
            currentTime = time
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.sdp, horizontal = 10.sdp)
            .clickable {
                // onTimeZoneClick(timezone)

            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.sdp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            verticalArrangement = Arrangement.spacedBy(6.sdp)
        ) {
            // Timezone name (e.g., Australia/ACT)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timezone,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    painter = painterResource(Res.drawable.ffavourite),
                    "",
                    tint = if (!isFavourite) Color.White else Color.Red,
                    modifier = Modifier.size(18.sdp).clickable {
                        onTimeZoneClick(timezone)
                    }
                )


            }


            // Offset (e.g., GMT+10:00)
            Text(
                text = offsetString,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Display name (e.g., Australian Eastern Standard Time)
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Current time (aligned right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


