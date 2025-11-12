package com.world.clock.screens.timeScrubber

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.screens.worldclock.FavouriteTimeZonesViewModel
import com.world.clock.utils.getGmtOffsetString
import com.world.clock.utils.getLocalDateFormatted
import com.world.clock.utils.getLocalDateFromUtc
import com.world.clock.utils.getLocalTimeFormatted
import com.world.clock.utils.getLocalTimeFromUtc
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.ic_back
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class TimeScrubberScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db: WorldClockDatabase = koinInject()
        val dao = remember { db.favouriteTimeZoneDao() }
        val viewModel = remember { FavouriteTimeZonesViewModel(dao) }
        val favouriteTimeZones by viewModel.timeZone.collectAsState()
        TimeScrubberScreenContent(timezones = favouriteTimeZones){
            navigator.pop()
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun TimeScrubberScreenContent(
    timezones: List<FavouriteTimeZone>,
    onBack: () -> Unit
) {
    val nowUtc = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    var selectedHour by remember { mutableStateOf(nowUtc.hour) }
    var selectedMinute by remember { mutableStateOf(nowUtc.minute) }

    val currentZone = TimeZone.currentSystemDefault()
    val currentItem = FavouriteTimeZone(
        dbId = -1L,
        id = currentZone.id,
        name = currentZone.id.substringAfterLast('/').replace('_', ' ')
    )

    val allZones = remember(timezones) {
        (listOf(currentItem) + timezones)
            .distinctBy { it.id }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.sdp),
    ) {

        // Header bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.sdp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier.size(24.sdp).align(Alignment.CenterStart).clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) {
                    onBack()
                }
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Time Scrubber",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        TimeScrubber(
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            onValueChange = { h, m ->
                selectedHour = h
                selectedMinute = m
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(allZones, key = { it.id + it.dbId.toString() }) { tz ->
                ScrubberTimeZoneCard(
                    timezone = tz,
                    utcHour = selectedHour,
                    utcMinute = selectedMinute
                )
            }
            item { Spacer(Modifier.height(25.sdp)) }
        }
    }
}

@Composable
fun TimeScrubber(
    selectedHour: Int,
    selectedMinute: Int,
    onValueChange: (hour: Int, minute: Int) -> Unit
) {
    val totalMinutes = selectedHour * 60 + selectedMinute
    val progress = (totalMinutes / 1440f).coerceIn(0f, 0.9999f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 6.sdp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.sdp),
        ) {
            Text("−", color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 16.ssp)

            Slider(
                value = progress,
                onValueChange = { p ->
                    val totalMins = (p * 1440).toInt().coerceAtMost(1439)
                    val hour = totalMins / 60
                    val minute = totalMins % 60
                    onValueChange(hour, minute)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(0.6f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(0.2f)
                ),
                modifier = Modifier.weight(1f)
            )

            Text("+", color = MaterialTheme.colorScheme.onSurface.copy(0.6f), fontSize = 16.ssp)
        }

        Text(
            text = "UTC ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 18.ssp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}



@OptIn(ExperimentalTime::class)
@Composable
fun ScrubberTimeZoneCard(
    timezone: FavouriteTimeZone,
    utcHour: Int,
    utcMinute: Int
) {
    val is24Hour by TimeFormatDatastore.is24HourFlow().collectAsState(false)
    val displayTime = getLocalTimeFromUtc(timezone.id, utcHour, utcMinute, is24Hour)
    val offset = getGmtOffsetString(TimeZone.of(timezone.id))
    val cityName = timezone.name.substringAfterLast('/').replace('_', ' ')

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.sdp),
        shape = RoundedCornerShape(16.sdp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = cityName, fontSize = 14.ssp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = offset,
                    fontSize = 12.ssp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.sdp)
                )
            }
            Text(
                text = displayTime,
                fontSize = 20.ssp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}