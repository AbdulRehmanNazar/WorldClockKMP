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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSizeDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
            onTimeZoneClick = { timeZoneId, timeZoneName ->
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
    onTimeZoneClick: (id: String, name: String) -> Unit
) {

    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }

    val filteredTimeZones = remember(searchQuery, timezones) {
        if (searchQuery.isBlank()) timezones
        else timezones.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
        verticalArrangement = Arrangement.spacedBy(8.sdp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.sdp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "World Clock",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold
            )
        }

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
            items(filteredTimeZones) { tz ->
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
                    horizontalArrangement = Arrangement.End
                ) {
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


