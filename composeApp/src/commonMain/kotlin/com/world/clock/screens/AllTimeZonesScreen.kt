package com.world.clock.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.utils.getGmtOffsetString
import com.world.clock.utils.getLocalTimeFormatted
import com.world.clock.utils.tickingClock
import com.world.clock.utils.timeZones
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.*
import kotlin.time.ExperimentalTime


class AllTimeZonesScreen() : Screen {


    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db: WorldClockDatabase = koinInject()
        val dao = remember { db.favouriteTimeZoneDao() }

        BackHandler(enabled = true) {
            navigator.pop()
        }


        AllTimeZonesScreenContent(
            timeZones,
            onTimeZoneClick = {timeZoneId, timeZoneName->
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insert(FavouriteTimeZone(id = timeZoneId, name = timeZoneName))
                }
                navigator.pop()
            },
        )
    }


}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AllTimeZonesScreenContent(
    timezones: List<FavouriteTimeZone>,
    onTimeZoneClick: (id:String, name:String) -> Unit
) {


    var searchQuery by remember { mutableStateOf("") }

    val filteredTimeZones = remember(searchQuery, timezones) {
        if (searchQuery.isBlank()) timezones
        else timezones.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                AllTimeZoneItem(
                    timezone = FavouriteTimeZone(id=tz.id, name=tz.name),
                    readOnly = true,
                    onTimeZoneClick = { dbId, timeZoneId, timeZoneName ->
                        onTimeZoneClick(timeZoneId, timeZoneName)

                    },
                    onUpdateName = { id, newName ->


                    })
            }
            item {
                Spacer(Modifier.height(25.sdp))
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AllTimeZoneItem(
    timezone: FavouriteTimeZone,
    isFavourite: Boolean = false,
    readOnly: Boolean = false,
    onUpdateName: (id: Long, newName: String) -> Unit,
    onTimeZoneClick: (dbId: Long,id:String, name:String) -> Unit
) {

    val is24Hour by TimeFormatDatastore.is24HourFlow().collectAsState(false)

    var currentTime by remember { mutableStateOf(getLocalTimeFormatted(timezone.id, is24Hour)) }

    val zone = remember(timezone) { TimeZone.of(timezone.id) }

    val offsetString = remember(zone) { getGmtOffsetString(zone) }

    val displayName = remember(timezone) {
        timezone.id.substringAfterLast('/').replace('_', ' ')
    }
    var timeZoneName = mutableStateOf(timezone.name)
    val focusManager = LocalFocusManager.current


    // Update ticking time every second
    LaunchedEffect(timezone, is24Hour) {
        tickingClock(timezone.id, is24Hour).collect { time ->
            currentTime = time
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.sdp)
            .clickable {
                focusManager.clearFocus()
                onTimeZoneClick(timezone.dbId ,timezone.id, timezone.name)
                // onTimeZoneClick(timezone)

            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.sdp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.sdp),
            verticalArrangement = Arrangement.spacedBy(6.sdp)
        ) {
            // Timezone name (e.g., Australia/ACT)
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 12.sdp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                    TextField(
                        value = timeZoneName.value,
                        onValueChange = {
                            timeZoneName.value = it

                        },
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onUpdateName(timezone.dbId, timeZoneName.value)
                                focusManager.clearFocus()
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            // containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .background(Color.Transparent)
                            .padding(0.sdp)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    onUpdateName(timezone.dbId, timeZoneName.value)
                                }
                            }

                    )


                Icon(
                    painter = painterResource(Res.drawable.ffavourite),
                    "",
                    tint = if (!isFavourite) Color.White else Color.Red,
                    modifier = Modifier.size(18.sdp).clickable {
                        onTimeZoneClick(timezone.dbId ,timezone.id, timezone.name)
                    }
                )


            }
            // Offset (e.g., GMT+10:00)
            Text(
                modifier = Modifier.padding(start = 12.sdp),
                text = offsetString,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Display name (e.g., Australian Eastern Standard Time)
            Text(
                modifier = Modifier.padding(start = 12.sdp),
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Current time (aligned right)
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 12.sdp),
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


