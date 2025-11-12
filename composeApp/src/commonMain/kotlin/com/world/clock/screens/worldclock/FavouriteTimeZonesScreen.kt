package com.world.clock.screens.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.screens.AllTimeZoneItem
import com.world.clock.screens.AllTimeZonesScreen
import com.world.clock.screens.settings.SettingsScreen
import com.world.clock.screens.timeScrubber.TimeScrubberScreen
import com.world.clock.utils.getGmtOffsetString
import com.world.clock.utils.getLocalDateFormatted
import com.world.clock.utils.getLocalTimeFormatted
import com.world.clock.utils.tickingClock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.ffavourite
import worldclockkmp.composeapp.generated.resources.ic_clock
import worldclockkmp.composeapp.generated.resources.ic_gear
import worldclockkmp.composeapp.generated.resources.ic_menu
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class FavouriteTimeZonesScreen() : Screen {


    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db: WorldClockDatabase = koinInject()
        val dao = remember { db.favouriteTimeZoneDao() }
        val viewModel = remember { FavouriteTimeZonesViewModel(dao) }
        val favouriteTimeZones by viewModel.timeZone.collectAsState()
        var menuDialog = remember { mutableStateOf(false) }

        FavouriteTimeZonesScreenContent(
            favouriteTimeZones,
            onAddClick = {
                navigator.push(AllTimeZonesScreen())
            },
            onMenuClick = {
                menuDialog.value = !menuDialog.value
            },
            onTimeZoneClick = { dbId, timeZoneId, timeZoneName ->
                viewModel.deleteTimeZone(dbId, timeZoneId, timeZoneName)
            }, { id, newName ->
                viewModel.updateTimeZone(id, newName)
            }
        )

        if (menuDialog.value) {
            Popup(
                offset = IntOffset(-40, 40),
                alignment = Alignment.TopEnd,
                onDismissRequest = { menuDialog.value = false }
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.sdp)
                        )
                        .padding(horizontal = 12.sdp, vertical = 6.sdp)
                        .width(120.sdp),
                    verticalArrangement = Arrangement.spacedBy(8.sdp)
                ) {
                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = MutableInteractionSource()
                        ) {
                            menuDialog.value = false
                            navigator.push(SettingsScreen())
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.sdp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_gear),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "Settings",
                            color = Color.White,
                            fontSize = 12.ssp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.sdp)
                        )
                    }

                    Row(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = MutableInteractionSource()
                        ) {
                            menuDialog.value = false
                            navigator.push(TimeScrubberScreen())
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.sdp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_clock),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "Time Scrubber",
                            color = Color.White,
                            fontSize = 12.ssp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.sdp)
                        )
                    }

                }
            }
        }
    }


}

@Composable
fun FavouriteTimeZonesScreenContent(
    timezones: List<FavouriteTimeZone>,
    onAddClick: () -> Unit,
    onMenuClick: () -> Unit,
    onTimeZoneClick: (dbId: Long, id: String, name: String) -> Unit,
    onUpdateName: (id: Long, newName: String) -> Unit
) {

    val currentTimeZone = TimeZone.currentSystemDefault()
    val currentTimeZoneItem = FavouriteTimeZone(
        dbId = -1L,
        id = currentTimeZone.id,
        name = currentTimeZone.id.substringAfterLast('/').replace('_', ' ')
    )
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery, timezones) {
        val combinedList = listOf(currentTimeZoneItem) + timezones
        if (searchQuery.isBlank()) combinedList
        else combinedList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    val focusManager = LocalFocusManager.current


//    val filteredList = remember(searchQuery, timezones) {
//        if (searchQuery.isBlank()) timezones
//        else timezones.filter { it.name.contains(searchQuery, ignoreCase = true) }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.sdp)
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource()
            ) {
                focusManager.clearFocus()
            },
    ) {

        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "World Clock",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold
            )


            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.sdp),
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clickable(
                            indication = null,
                            interactionSource = MutableInteractionSource()
                        ) { onAddClick() }
                        .padding(4.sdp)
                ) {
                    Text(
                        text = "\u2795",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.ssp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape
                        )
                        .clickable(
                            indication = null,
                            interactionSource = MutableInteractionSource()
                        ) {
                            onMenuClick()
                        }
                        .padding(4.sdp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_menu),
                        contentDescription = null
                    )
                }

            }
        }
Column(
    modifier = Modifier
        .fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.sdp)
) {
        // Search bar with elevated colorful look
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                        )
                    ),
                    shape = RoundedCornerShape(14.sdp)
                )
                .padding(2.sdp),
            placeholder = {
                Text(
                    "Search time zones...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            shape = RoundedCornerShape(12.sdp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 14.ssp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )


//        TimeFormatToggle()
        LazyColumn {
            items(filteredList) { tz ->
                if (tz.dbId == -1L) {
                    CurrentTimeZoneCard()
                } else {
                    AllTimeZoneItem(
                        timezone = tz,
                        isFavourite = true,
                        onTimeZoneClick = { dbId, timeZoneId, timeZoneName ->
                            onTimeZoneClick(dbId, timeZoneId, timeZoneName)
                        },
                        onUpdateName = { id, newName ->
                            onUpdateName(id, newName)
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(25.sdp)) }
        }
    }}
}

@OptIn(ExperimentalTime::class)
@Composable
fun CurrentTimeZoneCard() {
    val focusManager = LocalFocusManager.current
    val is24Hour by TimeFormatDatastore.is24HourFlow().collectAsState(false)
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val zoneId = timeZone.id.substringAfterLast('/').replace('_', ' ')
    var currentTime by remember { mutableStateOf(getLocalTimeFormatted(timeZone.id, is24Hour)) }
    val zone = remember(timeZone) { TimeZone.of(timeZone.id) }
    val date = getLocalDateFormatted(timeZone.id)
    val isDate by TimeFormatDatastore.isDateFlow().collectAsState(false)
    val offsetString = remember(zone) { getGmtOffsetString(zone) }
    LaunchedEffect(timeZone, is24Hour) {
        tickingClock(timeZone.id, is24Hour).collect { time ->
            currentTime = time
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.sdp),
        shape = RoundedCornerShape(20.sdp),
        elevation = CardDefaults.cardElevation(8.sdp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(20.sdp)
                )
                .padding(top = 14.sdp, bottom = 16.sdp, start = 8.sdp, end = 16.sdp)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.sdp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextField(
                        value = timeZone.id,
                        onValueChange = { },
                        readOnly = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.sdp),
                    )

                    Text(
                        modifier = Modifier,
                        text = "Current Time Zone",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

//                    Icon(
//                        painter = painterResource(Res.drawable.ffavourite),
//                        contentDescription = null,
//                        tint = if (!isFavourite)
//                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
//                        else
//                            MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
//                        modifier = Modifier
//                            .size(24.sdp)
//                            .clickable {
//                                onTimeZoneClick(timezone.dbId, timezone.id, timezone.name)
//                            }
//                    )
                }

                // GMT Offset Chip
                Row(
                    modifier = Modifier
                        .padding(start = 10.sdp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                            RoundedCornerShape(12.sdp)
                        )
                        .padding(horizontal = 10.sdp, vertical = 6.sdp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offsetString,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Display name
                Text(
                    text = zoneId,
                    modifier = Modifier.padding(start = 12.sdp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    modifier = Modifier.padding(start = 12.sdp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(start = 12.sdp),
                        text = if (isDate) date else "",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
            .fillMaxWidth()
            .padding(vertical = 4.sdp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.sdp)
            )
            .padding(horizontal = 12.sdp, vertical = 6.sdp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "24-Hour Format",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.ssp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )

        Switch(
            checked = is24Hour,
            onCheckedChange = {
                scope.launch { TimeFormatDatastore.set24Hour(it) }
            },
            thumbContent = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}
