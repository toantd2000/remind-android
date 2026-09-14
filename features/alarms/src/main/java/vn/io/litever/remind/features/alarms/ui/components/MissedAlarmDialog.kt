package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.PowerOff
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun MissedAlarmDialog(
    missedAlarms: List<MissedAlarm>,
    onDismiss: () -> Unit
) {
    LvAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.missed_alarms_dialog_title)) },
        confirmButton = {
            LvButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.missed_alarms_dialog_desc),
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    items(missedAlarms) { missed ->
                        MissedAlarmItem(missed)
                    }
                }
            }
        }
    )
}

@Composable
private fun MissedAlarmItem(missed: MissedAlarm) {
    val dateTime = remember(missed.scheduledTime) {
        val instant = Instant.ofEpochMilli(missed.scheduledTime)
        val zoneId = try {
            ZoneId.systemDefault()
        } catch (_: Exception) {
            ZoneId.of("UTC")
        }
        LocalDateTime.ofInstant(instant, zoneId)
    }
    val timeFormatter = remember {
        try {
            DateTimeFormatter.ofPattern("HH:mm, dd MMM")
        } catch (e: Exception) {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        }
    }

    val icon: ImageVector
    val reasonText: String
    when (missed.reason) {
        MissedReason.TIMEOUT -> {
            icon = Icons.Rounded.NotificationsPaused
            reasonText = stringResource(R.string.missed_reason_timeout)
        }
        MissedReason.POWER_OFF -> {
            icon = Icons.Rounded.PowerOff
            reasonText = stringResource(R.string.missed_reason_power_off)
        }
        MissedReason.PERMISSION_MISSING -> {
            icon = Icons.Rounded.Security
            reasonText = stringResource(R.string.missed_reason_permission)
        }
        MissedReason.UNKNOWN -> {
            icon = Icons.Rounded.Warning
            reasonText = stringResource(R.string.missed_reason_unknown)
        }
    }

    Surface(
        color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.5f),
        shape = LiteverTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.smallMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(LiteverTheme.spacing.large),
                tint = LiteverTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))
            Column {
                val labelText = missed.alarmLabel.ifEmpty {
                    stringResource(R.string.no_label)
                }
                Text(
                    text = labelText,
                    style = LiteverTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (missed.alarmLabel.isEmpty()) FontStyle.Italic else FontStyle.Normal
                )
                Text(
                    text = "${dateTime.format(timeFormatter)} • $reasonText",
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun MissedAlarmDialogPreview() {
    ReMindTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            MissedAlarmDialog(
                missedAlarms = listOf(
                    MissedAlarm(
                        id = 1,
                        alarmId = 101,
                        alarmLabel = "Morning Yoga",
                        scheduledTime = System.currentTimeMillis() - 3600000,
                        missedTime = System.currentTimeMillis() - 3540000,
                        reason = MissedReason.POWER_OFF
                    ),
                    MissedAlarm(
                        id = 2,
                        alarmId = 102,
                        alarmLabel = "Medicine",
                        scheduledTime = System.currentTimeMillis() - 7200000,
                        missedTime = System.currentTimeMillis() - 7140000,
                        reason = MissedReason.PERMISSION_MISSING
                    ),
                    MissedAlarm(
                        id = 3,
                        alarmId = 103,
                        alarmLabel = "",
                        scheduledTime = System.currentTimeMillis() - 10800000,
                        missedTime = System.currentTimeMillis() - 10740000,
                        reason = MissedReason.TIMEOUT
                    )
                ),
                onDismiss = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun MissedAlarmDialogDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            MissedAlarmDialog(
                missedAlarms = listOf(
                    MissedAlarm(
                        id = 1,
                        alarmId = 101,
                        alarmLabel = "Quick Nap",
                        scheduledTime = System.currentTimeMillis() - 1800000,
                        missedTime = System.currentTimeMillis() - 1740000,
                        reason = MissedReason.UNKNOWN
                    )
                ),
                onDismiss = {}
            )
        }
    }
}
