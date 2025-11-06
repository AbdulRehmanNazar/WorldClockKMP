package com.world.clock.screens.worldclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.data.getDatabaseBuilder
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.screens.AllTimeZoneItem
import com.world.clock.screens.AllTimeZonesScreen
import com.world.clock.utils.tickingClock
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp


class FavouriteTimeZonesScreen() : Screen {


    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db = remember { getDatabaseBuilder() }
        val dao = remember { db.favouriteTimeZoneDao() }
        val viewModel = remember { FavouriteTimeZonesViewModel(dao) }
        val favouriteTimeZones by viewModel.timeZone.collectAsState()

        FavouriteTimeZonesScreenContent(
            favouriteTimeZones,
            onAddClick = {
                navigator.push(AllTimeZonesScreen())
            },
            onTimeZoneClick = { timeZone ->
                viewModel.deleteTimeZone(timeZone)
            },
        )
    }


}

@Composable
fun FavouriteTimeZonesScreenContent(
    timezones: List<FavouriteTimeZone>,
    onAddClick: () -> Unit,
    onTimeZoneClick: (String) -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, timezones) {
        if (searchQuery.isBlank()) timezones
        else timezones.filter { it.id.contains(searchQuery, ignoreCase = true) }
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
                fontSize = 20.ssp,
                modifier = Modifier.clickable {
                    onAddClick()
                })
            Text(
                text = "\u2795",
                color = Color.Black,
                fontSize = 20.ssp,
                modifier = Modifier.clickable {
                    onAddClick()
                })
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search time zones...") },
            singleLine = true
        )
        TimeFormatToggle()
        LazyColumn {
            items(filteredList) { tz ->
                AllTimeZoneItem(
                    timezone = tz, isFavourite = true, onTimeZoneClick = { timeZone ->

                        onTimeZoneClick(timeZone)
                    },
                    onUpdateName = { id, newName ->


                    }
                )
            }
        }
    }
}


@Composable
fun TimeFormatToggle() {
    val scope = rememberCoroutineScope()
    val is24Hour by TimeFormatDatastore.is24HourFlow()
        .collectAsState(initial = false)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("24-Hour Format", style = MaterialTheme.typography.bodyMedium, fontSize = 14.ssp)
        Switch(
            checked = is24Hour,
            onCheckedChange = {
                scope.launch {
                    TimeFormatDatastore.set24Hour(it)
                }
            }
        )
    }
}
