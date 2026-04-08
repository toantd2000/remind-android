package vn.io.litever.alarm.features.alarm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.alarm.core.designsystem.components.AlarmSwitch
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.core.common.util.TimeFormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    is24HourFormat: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (alarm.isEnabled) 1f else 0.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { this.alpha = alpha }
            ) {
                // Top: Repeat Info
                Text(
                    text = getRepeatText(alarm),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Middle: Time
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(alarm.time, is24HourFormat)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (amPm != null) {
                        Text(
                            text = " $amPm",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                
                // Bottom: Label (Reserved space, 1 line)
                Text(
                    text = alarm.label.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AlarmSwitch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun getRepeatText(alarm: Alarm): String {
    return getRepeatSummaryText(alarm.repeatDays, alarm.time)
}
