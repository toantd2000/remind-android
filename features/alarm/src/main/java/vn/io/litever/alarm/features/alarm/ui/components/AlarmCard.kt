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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
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
                Text(
                    text = alarm.time.format(timeFormatter),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

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
    if (alarm.repeatDays.isEmpty()) {
        val now = java.time.LocalTime.now()
        return if (alarm.time.isAfter(now)) {
            stringResource(R.string.today)
        } else {
            stringResource(R.string.tomorrow)
        }
    }
    
    if (alarm.repeatDays.size == 7) {
        return stringResource(R.string.every_day)
    }
    val context = LocalContext.current
    return alarm.repeatDays.joinToString(", ") { day ->
        when (day) {
            DayOfWeek.MONDAY -> context.getString(R.string.day_mon)
            DayOfWeek.TUESDAY -> context.getString(R.string.day_tue)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.day_wed)
            DayOfWeek.THURSDAY -> context.getString(R.string.day_thu)
            DayOfWeek.FRIDAY -> context.getString(R.string.day_fri)
            DayOfWeek.SATURDAY -> context.getString(R.string.day_sat)
            DayOfWeek.SUNDAY -> context.getString(R.string.day_sun)
        }
    }
}
