package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderWifiConfigSection(
    ssid: String,
    onSsidChange: (String) -> Unit,
    bssid: String?,
    onBssidChange: (String?) -> Unit,
    triggerType: WifiTriggerType,
    onTriggerTypeChange: (WifiTriggerType) -> Unit,
    timeWindow: ReminderTimeWindow?,
    onTimeWindowChange: (ReminderTimeWindow?) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var showTimeWindowDialog by remember { mutableStateOf(false) }

    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_wifi_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // SSID
            LiteverTextField(
                value = ssid,
                onValueChange = onSsidChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.wifi_ssid_placeholder)) },
                label = { Text(stringResource(R.string.wifi_ssid_label)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // BSSID
            LiteverTextField(
                value = bssid ?: "",
                onValueChange = { onBssidChange(it.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.wifi_bssid_placeholder)) },
                label = { Text(stringResource(R.string.wifi_bssid_label)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wi-Fi Trigger Event Type
            Text(
                text = stringResource(R.string.wifi_trigger_type_label),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WifiTriggerType.entries.forEach { type ->
                    val isSelected = type == triggerType
                    val labelRes = when (type) {
                        WifiTriggerType.ON_CONNECT -> R.string.wifi_on_connect
                        WifiTriggerType.ON_DISCONNECT -> R.string.wifi_on_disconnect
                        WifiTriggerType.WHILE_CONNECTED -> R.string.wifi_while_connected
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onTriggerTypeChange(type) },
                        label = { Text(stringResource(labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LiteverTheme.colors.primary,
                            selectedLabelColor = LiteverTheme.colors.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Defined Time Window Trigger
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showTimeWindowDialog = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = null,
                            tint = LiteverTheme.colors.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.time_window_section_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            val summary = if (timeWindow != null) {
                                val s = TimeFormatUtils.formatTime(timeWindow.startTime, is24HourFormat)
                                val e = TimeFormatUtils.formatTime(timeWindow.endTime, is24HourFormat)
                                "$s - $e"
                            } else {
                                stringResource(R.string.time_window_desc)
                            }
                            Text(
                                text = summary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTimeWindowDialog) {
        ReminderTimeWindowDialog(
            initialTimeWindow = timeWindow,
            is24HourFormat = is24HourFormat,
            onDismissRequest = { showTimeWindowDialog = false },
            onConfirm = { newWindow ->
                onTimeWindowChange(newWindow)
                showTimeWindowDialog = false
            }
        )
    }
}
