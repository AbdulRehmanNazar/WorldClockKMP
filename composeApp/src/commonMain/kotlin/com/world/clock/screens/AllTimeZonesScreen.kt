package com.world.clock.screens

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
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.entity.FavouriteTimeZone
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.screens.settings.SettingsScreen
import com.world.clock.utils.getGmtOffsetString
import com.world.clock.utils.getLocalDateFormatted
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
import org.koin.compose.koinInject
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class AllTimeZonesScreen() : Screen {


    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val db: WorldClockDatabase = koinInject()
        val dao = remember { db.favouriteTimeZoneDao() }
        var menuDialog = remember { mutableStateOf(false) }


        BackHandler(enabled = true) {
            navigator.pop()
        }


        AllTimeZonesScreenContent(
            timeZones,
            onMenuClick = {
                menuDialog.value = !menuDialog.value
            },
            onTimeZoneClick = { timeZoneId, timeZoneName ->
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insert(FavouriteTimeZone(id = timeZoneId, name = timeZoneName))
                }
                navigator.pop()
            },
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
                        .width(100.sdp),
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
                            fontSize = 14.ssp,
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


@OptIn(ExperimentalComposeUiApi::class, ExperimentalTime::class)
@Composable
fun AllTimeZonesScreenContent(
    timezones: List<FavouriteTimeZone>,
    onMenuClick: () -> Unit,
    onTimeZoneClick: (id: String, name: String) -> Unit
) {

    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(SortType.REGION) }
    var showSortDialog by remember { mutableStateOf(false) }



    val filteredTimeZones = remember(searchQuery, timezones) {
        if (searchQuery.isBlank()) timezones
        else timezones.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val sortedTimeZones = remember(filteredTimeZones, sortType) {
        when (sortType) {
            SortType.REGION -> filteredTimeZones.sortedBy { it.name.substringAfter('/').lowercase() }
            SortType.GMT_OFFSET -> filteredTimeZones.sortedBy {
                val zone = TimeZone.of(it.id)
                zone.offsetAt(Clock.System.now()).totalSeconds
            }
//            SortType.CITIES -> filteredTimeZones.sortedBy { it.name.substring() }
            SortType.CITIES -> filteredTimeZones.sortedBy { it.name.substringBefore('/').lowercase() }
        }
    }


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
//                SortDropdown(sortType = sortType, onSortChange = { sortType = it })

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
                            showSortDialog = true
                        }
                        .padding(6.sdp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_sort),
                        contentDescription = null,
                        modifier = Modifier.size(20.sdp),
                        tint = Color.Black
                    )
                }

//                Box(
//                    modifier = Modifier
//                        .background(
//                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
//                            shape = CircleShape
//                        )
//                        .clickable(
//                            indication = null,
//                            interactionSource = MutableInteractionSource()
//                        ) { onAddClick() }
//                        .padding(4.sdp)
//                ) {
//                    Text(
//                        text = "\u2795",
//                        color = MaterialTheme.colorScheme.primary,
//                        fontSize = 16.ssp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
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
                        tint = Color.Black,
                        modifier = Modifier.size(20.sdp),
                        contentDescription = null
                    )
                }

            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.sdp)
        ) {

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


            LazyColumn {
                items(sortedTimeZones) { tz ->
                    AllTimeZoneItem(
                        timezone = FavouriteTimeZone(id = tz.id, name = tz.name),
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

        if (showSortDialog) {
            SortDialog(
                selectedSort = sortType,
                onDismiss = { showSortDialog = false },
                onSortSelected = {
                    sortType = it
                    showSortDialog = false
                }
            )
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
    onTimeZoneClick: (dbId: Long, id: String, name: String) -> Unit
) {

    val is24Hour by TimeFormatDatastore.is24HourFlow().collectAsState(false)
    val isDate by TimeFormatDatastore.isDateFlow().collectAsState(false)

    var currentTime by remember { mutableStateOf(getLocalTimeFormatted(timezone.id, is24Hour)) }

    val date = getLocalDateFormatted(timezone.id)

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
                        value = timeZoneName.value,
                        onValueChange = { timeZoneName.value = it },
                        readOnly = readOnly,
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
                            .padding(0.sdp)
                            .onFocusChanged { focus ->
                                if (!focus.isFocused) {
                                    onUpdateName(timezone.dbId, timeZoneName.value)
                                }
                            },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onUpdateName(timezone.dbId, timeZoneName.value)
                                focusManager.clearFocus()
                            }
                        )
                    )

                    Icon(
                        painter = painterResource(Res.drawable.ffavourite),
                        contentDescription = null,
                        tint = if (!isFavourite)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(24.sdp)
                            .clickable {
                                onTimeZoneClick(timezone.dbId, timezone.id, timezone.name)
                            }
                    )
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
                    text = displayName,
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
fun SortDialog(
    selectedSort: SortType,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort By",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
            ) {
                SortType.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedSort,
                            onClick = { onSortSelected(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = option.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.sdp
    )
}


enum class SortType(val displayName: String) {
    REGION("Region"),
    GMT_OFFSET("GMT Offset"),
    CITIES("City")
}


