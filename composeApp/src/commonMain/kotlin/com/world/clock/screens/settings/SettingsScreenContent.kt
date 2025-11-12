package com.world.clock.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.world.clock.datastore.TimeFormatDatastore
import com.world.clock.screens.worldclock.TimeFormatToggle
import kotlinx.coroutines.launch
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp
import org.jetbrains.compose.resources.painterResource
import worldclockkmp.composeapp.generated.resources.Res
import worldclockkmp.composeapp.generated.resources.ic_back

@Composable
fun SettingsScreenContent(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isDate by TimeFormatDatastore.isDateFlow()
        .collectAsState(initial = false)
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
            Text(modifier = Modifier.align(Alignment.Center),
                text = "Settings",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.ssp,
                fontWeight = FontWeight.ExtraBold
            )
        }
Column(
    modifier = Modifier
        .fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.sdp)
) {
    TimeFormatToggle()

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
            text = "Display Date",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.ssp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )

        Switch(
            checked = isDate,
            onCheckedChange = {
                scope.launch { TimeFormatDatastore.setDate(it) }
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
    }
}